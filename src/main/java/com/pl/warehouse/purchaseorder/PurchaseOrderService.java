package com.pl.warehouse.purchaseorder;

import com.pl.warehouse.common.exception.ConflictException;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.inventory.InventoryService;
import com.pl.warehouse.item.ItemRepository;
import com.pl.warehouse.item.WarehouseItem;
import com.pl.warehouse.purchaseorder.dto.PurchaseOrderLineRequest;
import com.pl.warehouse.purchaseorder.dto.PurchaseOrderLineResponse;
import com.pl.warehouse.purchaseorder.dto.PurchaseOrderRequest;
import com.pl.warehouse.purchaseorder.dto.PurchaseOrderResponse;
import com.pl.warehouse.purchaseorder.dto.ReceivePurchaseOrderRequest;
import com.pl.warehouse.supplier.Supplier;
import com.pl.warehouse.supplier.SupplierRepository;
import com.pl.warehouse.warehouse.Warehouse;
import com.pl.warehouse.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * STUDY (mid-level CRM) — purchase order lifecycle + receiving into stock.
 *
 * <p>{@code receive} is the showcase {@code @Transactional}: update many PO lines,
 * bump inventory, insert movements — all commit or all roll back.
 */
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(UUID id) {
        return toDetail(findDetailed(id));
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        if (purchaseOrderRepository.existsByPoNumberIgnoreCase(request.poNumber())) {
            throw new ConflictException("PO number already exists: " + request.poNumber());
        }
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(request.poNumber().trim());
        po.setSupplier(supplier);
        po.setWarehouse(warehouse);
        po.setStatus(PurchaseOrderStatus.DRAFT);
        po.setExpectedAt(request.expectedAt());
        po.setNotes(request.notes());

        for (PurchaseOrderLineRequest lineReq : request.lines()) {
            WarehouseItem item = itemRepository.findById(lineReq.itemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", lineReq.itemId()));
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setItem(item);
            line.setQuantityOrdered(lineReq.quantityOrdered());
            line.setQuantityReceived(0);
            line.setUnitCost(lineReq.unitCost());
            po.addLine(line);
        }

        return toDetail(purchaseOrderRepository.save(po));
    }

    /** DRAFT → ORDERED (locks editing in a real system; here we just stamp status). */
    @Transactional
    public PurchaseOrderResponse submit(UUID id) {
        PurchaseOrder po = findDetailed(id);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT POs can be submitted");
        }
        po.setStatus(PurchaseOrderStatus.ORDERED);
        po.setOrderedAt(Instant.now());
        return toDetail(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse cancel(UUID id) {
        PurchaseOrder po = findDetailed(id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new IllegalArgumentException("Fully received PO cannot be cancelled");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return toDetail(purchaseOrderRepository.save(po));
    }

    /**
     * Receive goods: increase line.received + inventory in the PO warehouse.
     * Partial receives allowed until all lines are complete → RECEIVED.
     */
    @Transactional
    public PurchaseOrderResponse receive(UUID id, ReceivePurchaseOrderRequest request) {
        PurchaseOrder po = findDetailed(id);
        if (po.getStatus() == PurchaseOrderStatus.DRAFT
                || po.getStatus() == PurchaseOrderStatus.CANCELLED
                || po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new IllegalArgumentException(
                    "Cannot receive PO in status " + po.getStatus()
            );
        }

        Map<UUID, PurchaseOrderLine> byId = new HashMap<>();
        for (PurchaseOrderLine line : po.getLines()) {
            byId.put(line.getId(), line);
        }

        for (ReceivePurchaseOrderRequest.ReceiveLine recv : request.lines()) {
            PurchaseOrderLine line = byId.get(recv.lineId());
            if (line == null) {
                throw new ResourceNotFoundException("PurchaseOrderLine", recv.lineId());
            }
            if (recv.quantity() > line.remaining()) {
                throw new IllegalArgumentException(
                        "Receive qty exceeds remaining for line " + line.getId()
                                + " remaining=" + line.remaining()
                );
            }
            line.setQuantityReceived(line.getQuantityReceived() + recv.quantity());
            inventoryService.receiveIntoWarehouse(
                    po.getWarehouse().getId(),
                    line.getItem().getId(),
                    recv.quantity(),
                    "PO receive " + po.getPoNumber()
            );
        }

        boolean allDone = po.getLines().stream().allMatch(l -> l.remaining() == 0);
        boolean anyReceived = po.getLines().stream().anyMatch(l -> l.getQuantityReceived() > 0);
        if (allDone) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PurchaseOrderStatus.PARTIAL);
        }

        return toDetail(purchaseOrderRepository.save(po));
    }

    private PurchaseOrder findDetailed(UUID id) {
        return purchaseOrderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    private PurchaseOrderResponse toSummary(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getWarehouse().getId(),
                po.getWarehouse().getName(),
                po.getStatus().name(),
                po.getOrderedAt(),
                po.getExpectedAt(),
                po.getNotes(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                List.of()
        );
    }

    private PurchaseOrderResponse toDetail(PurchaseOrder po) {
        List<PurchaseOrderLineResponse> lines = po.getLines().stream()
                .map(l -> new PurchaseOrderLineResponse(
                        l.getId(),
                        l.getItem().getId(),
                        l.getItem().getItemName(),
                        l.getQuantityOrdered(),
                        l.getQuantityReceived(),
                        l.remaining(),
                        l.getUnitCost()
                ))
                .toList();
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getWarehouse().getId(),
                po.getWarehouse().getName(),
                po.getStatus().name(),
                po.getOrderedAt(),
                po.getExpectedAt(),
                po.getNotes(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                lines
        );
    }
}
