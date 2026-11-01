package com.pl.warehouse.inventory;

import com.pl.warehouse.common.event.StockChangedEvent;
import com.pl.warehouse.common.exception.ConflictException;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.inventory.dto.InventoryRequest;
import com.pl.warehouse.inventory.dto.InventoryResponse;
import com.pl.warehouse.inventory.dto.ReorderSuggestionResponse;
import com.pl.warehouse.inventory.dto.StockAdjustRequest;
import com.pl.warehouse.inventory.dto.StockMovementResponse;
import com.pl.warehouse.inventory.dto.StockTransferRequest;
import com.pl.warehouse.item.ItemRepository;
import com.pl.warehouse.item.WarehouseItem;
import com.pl.warehouse.warehouse.Warehouse;
import com.pl.warehouse.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * STUDY — Warehouse CRM stock operations (rules + orchestration).
 *
 * <p>Beyond CRUD: adjust, transfer, PO receive helper, reservations hooks,
 * reorder suggestions, movement history + {@link StockChangedEvent}.
 *
 * <p>{@code @Transactional}: multi-write use-cases commit together.
 * {@code readOnly}: keep persistence context open for LAZY mapping.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getById(UUID id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getByWarehouse(UUID warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock(UUID warehouseId) {
        return inventoryRepository.findLowStockByWarehouse(warehouseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReorderSuggestionResponse> reorderSuggestions() {
        List<ReorderSuggestionResponse> result = new ArrayList<>();
        for (Inventory inv : inventoryRepository.findAll()) {
            if (inv.getQuantity() < inv.getMinQuantity()) {
                result.add(new ReorderSuggestionResponse(
                        inv.getId(),
                        inv.getWarehouse().getId(),
                        inv.getWarehouse().getName(),
                        inv.getItem().getId(),
                        inv.getItem().getItemName(),
                        inv.getQuantity(),
                        inv.getMinQuantity(),
                        inv.getMinQuantity() - inv.getQuantity(),
                        inv.getItem().getSupplier().getName()
                ));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> recentMovements(int limit) {
        int size = Math.min(Math.max(limit, 1), 100);
        return stockMovementRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size))
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        Warehouse warehouse = requireWarehouse(request.warehouseId());
        WarehouseItem item = requireItem(request.itemId());

        inventoryRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Stock already exists for this warehouse and item. "
                                    + "Use Save stock (upsert) or Adjust. Existing id: " + existing.getId()
                    );
                });

        Inventory inventory = new Inventory();
        apply(inventory, request, warehouse, item);
        Inventory saved = inventoryRepository.save(inventory);
        recordMovement(saved, StockMovementType.SET, saved.getQuantity(), request.location(), null);
        return toResponse(saved);
    }

    @Transactional
    public InventoryResponse upsert(InventoryRequest request) {
        Warehouse warehouse = requireWarehouse(request.warehouseId());
        WarehouseItem item = requireItem(request.itemId());

        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .orElseGet(Inventory::new);

        int before = inventory.getId() == null ? 0 : inventory.getQuantity();
        apply(inventory, request, warehouse, item);
        if (inventory.getReservedQuantity() > inventory.getQuantity()) {
            throw new IllegalArgumentException(
                    "quantity cannot be below reservedQuantity (" + inventory.getReservedQuantity() + ")"
            );
        }
        Inventory saved = inventoryRepository.save(inventory);
        recordMovement(
                saved,
                StockMovementType.SET,
                saved.getQuantity() - before,
                request.location() != null ? "Upsert: " + request.location() : "Upsert stock",
                null
        );
        return toResponse(saved);
    }

    @Transactional
    public InventoryResponse adjust(UUID inventoryId, StockAdjustRequest request) {
        if (request.delta() == 0) {
            throw new IllegalArgumentException("delta must not be 0");
        }
        Inventory inventory = find(inventoryId);
        int next = inventory.getQuantity() + request.delta();
        if (next < 0) {
            throw new IllegalArgumentException(
                    "Not enough stock. On hand=" + inventory.getQuantity() + ", delta=" + request.delta()
            );
        }
        if (request.delta() < 0 && inventory.available() + request.delta() < 0) {
            throw new IllegalArgumentException(
                    "Not enough available stock (on hand - reserved). available="
                            + inventory.available() + ", delta=" + request.delta()
            );
        }
        inventory.setQuantity(next);
        Inventory saved = inventoryRepository.save(inventory);

        StockMovementType type = request.delta() > 0 ? StockMovementType.RECEIVE : StockMovementType.SHIP;
        if (request.reason() != null && request.reason().toLowerCase().contains("adjust")) {
            type = StockMovementType.ADJUST;
        }
        recordMovement(saved, type, request.delta(), request.reason(), null);
        return toResponse(saved);
    }

    /**
     * STUDY: Used by PurchaseOrderService.receive — find-or-create stock row and +qty.
     * Must run inside the caller's transaction (default REQUIRED joins it).
     */
    @Transactional
    public InventoryResponse receiveIntoWarehouse(
            UUID warehouseId,
            UUID itemId,
            int quantity,
            String reason
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("receive quantity must be > 0");
        }
        Warehouse warehouse = requireWarehouse(warehouseId);
        WarehouseItem item = requireItem(itemId);

        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseGet(() -> {
                    Inventory created = new Inventory();
                    created.setWarehouse(warehouse);
                    created.setItem(item);
                    created.setQuantity(0);
                    created.setMinQuantity(0);
                    created.setReservedQuantity(0);
                    return created;
                });

        inventory.setQuantity(inventory.getQuantity() + quantity);
        Inventory saved = inventoryRepository.save(inventory);
        recordMovement(saved, StockMovementType.RECEIVE, quantity, reason, null);
        return toResponse(saved);
    }

    /** Called by reservation service — increases reserved without changing on-hand. */
    @Transactional
    public Inventory reserve(UUID warehouseId, UUID itemId, int quantity) {
        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock to reserve for warehouse/item"
                ));
        if (inventory.available() < quantity) {
            throw new IllegalArgumentException(
                    "Not enough available stock to reserve. available=" + inventory.available()
            );
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory releaseReservation(UUID warehouseId, UUID itemId, int quantity) {
        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock row for warehouse/item"
                ));
        int next = inventory.getReservedQuantity() - quantity;
        if (next < 0) {
            throw new IllegalArgumentException("Cannot release more than reserved");
        }
        inventory.setReservedQuantity(next);
        return inventoryRepository.save(inventory);
    }

    /**
     * Fulfill reservation: release hold and ship (reduce on-hand) in one go.
     */
    @Transactional
    public InventoryResponse fulfillReservation(UUID warehouseId, UUID itemId, int quantity, String reason) {
        Inventory inventory = releaseReservation(warehouseId, itemId, quantity);
        int next = inventory.getQuantity() - quantity;
        if (next < 0) {
            throw new IllegalArgumentException("Not enough on-hand to fulfill");
        }
        inventory.setQuantity(next);
        Inventory saved = inventoryRepository.save(inventory);
        recordMovement(saved, StockMovementType.SHIP, -quantity, reason, null);
        return toResponse(saved);
    }

    @Transactional
    public List<InventoryResponse> transfer(StockTransferRequest request) {
        if (request.fromWarehouseId().equals(request.toWarehouseId())) {
            throw new IllegalArgumentException("fromWarehouseId and toWarehouseId must differ");
        }

        Warehouse fromWh = requireWarehouse(request.fromWarehouseId());
        Warehouse toWh = requireWarehouse(request.toWarehouseId());
        WarehouseItem item = requireItem(request.itemId());

        Inventory from = inventoryRepository
                .findByWarehouseIdAndItemId(fromWh.getId(), item.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No stock for item in source warehouse"
                ));

        if (from.available() < request.quantity()) {
            throw new IllegalArgumentException(
                    "Not enough available stock to transfer. available=" + from.available()
            );
        }

        from.setQuantity(from.getQuantity() - request.quantity());
        Inventory savedFrom = inventoryRepository.save(from);

        Inventory to = inventoryRepository
                .findByWarehouseIdAndItemId(toWh.getId(), item.getId())
                .orElseGet(() -> {
                    Inventory created = new Inventory();
                    created.setWarehouse(toWh);
                    created.setItem(item);
                    created.setQuantity(0);
                    created.setMinQuantity(from.getMinQuantity());
                    created.setReservedQuantity(0);
                    created.setLocation(from.getLocation());
                    created.setWarehouseSection(from.getWarehouseSection());
                    return created;
                });
        to.setQuantity(to.getQuantity() + request.quantity());
        Inventory savedTo = inventoryRepository.save(to);

        String reason = request.reason() != null ? request.reason() : "Transfer";
        recordMovement(savedFrom, StockMovementType.TRANSFER_OUT, -request.quantity(), reason, toWh);
        recordMovement(savedTo, StockMovementType.TRANSFER_IN, request.quantity(), reason, fromWh);

        return List.of(toResponse(savedFrom), toResponse(savedTo));
    }

    @Transactional
    public InventoryResponse update(UUID id, InventoryRequest request) {
        Inventory inventory = find(id);
        Warehouse warehouse = requireWarehouse(request.warehouseId());
        WarehouseItem item = requireItem(request.itemId());

        inventoryRepository.findByWarehouseIdAndItemId(warehouse.getId(), item.getId())
                .ifPresent(other -> {
                    if (!other.getId().equals(id)) {
                        throw new ConflictException(
                                "Another stock row already uses this warehouse + item pair."
                        );
                    }
                });

        int before = inventory.getQuantity();
        apply(inventory, request, warehouse, item);
        if (inventory.getReservedQuantity() > inventory.getQuantity()) {
            throw new IllegalArgumentException(
                    "quantity cannot be below reservedQuantity (" + inventory.getReservedQuantity() + ")"
            );
        }
        Inventory saved = inventoryRepository.save(inventory);
        recordMovement(saved, StockMovementType.SET, saved.getQuantity() - before, "Update stock", null);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Inventory inventory = find(id);
        if (inventory.getReservedQuantity() > 0) {
            throw new ConflictException("Cannot delete inventory with active reservations");
        }
        inventoryRepository.delete(inventory);
    }

    private void recordMovement(
            Inventory inventory,
            StockMovementType type,
            int delta,
            String reason,
            Warehouse related
    ) {
        StockMovement movement = new StockMovement();
        movement.setInventoryId(inventory.getId());
        movement.setWarehouse(inventory.getWarehouse());
        movement.setItem(inventory.getItem());
        movement.setMovementType(type);
        movement.setQuantityDelta(delta);
        movement.setQuantityAfter(inventory.getQuantity());
        movement.setReason(reason);
        movement.setRelatedWarehouse(related);
        stockMovementRepository.save(movement);

        events.publishEvent(new StockChangedEvent(
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getItem().getId(),
                delta,
                inventory.getQuantity(),
                reason
        ));
    }

    private Inventory find(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
    }

    private Warehouse requireWarehouse(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    private WarehouseItem requireItem(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    private void apply(
            Inventory inventory,
            InventoryRequest request,
            Warehouse warehouse,
            WarehouseItem item
    ) {
        inventory.setWarehouse(warehouse);
        inventory.setItem(item);
        inventory.setQuantity(request.quantity());
        inventory.setMinQuantity(request.minQuantity());
        inventory.setLocation(request.location());
        inventory.setWarehouseSection(request.warehouseSection());
        if (inventory.getReservedQuantity() == null) {
            inventory.setReservedQuantity(0);
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                inventory.getItem().getId(),
                inventory.getItem().getItemName(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.available(),
                inventory.getMinQuantity(),
                inventory.getVersion(),
                inventory.getLocation(),
                inventory.getWarehouseSection(),
                inventory.getUpdatedAt(),
                inventory.getQuantity() < inventory.getMinQuantity()
        );
    }

    private StockMovementResponse toMovementResponse(StockMovement m) {
        return new StockMovementResponse(
                m.getId(),
                m.getWarehouse().getId(),
                m.getWarehouse().getName(),
                m.getItem().getId(),
                m.getItem().getItemName(),
                m.getMovementType().name(),
                m.getQuantityDelta(),
                m.getQuantityAfter(),
                m.getReason(),
                m.getRelatedWarehouse() != null ? m.getRelatedWarehouse().getId() : null,
                m.getRelatedWarehouse() != null ? m.getRelatedWarehouse().getName() : null,
                m.getCreatedAt()
        );
    }
}
