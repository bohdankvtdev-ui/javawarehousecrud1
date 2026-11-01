package com.pl.warehouse.reservation;

import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.inventory.InventoryService;
import com.pl.warehouse.item.ItemRepository;
import com.pl.warehouse.item.WarehouseItem;
import com.pl.warehouse.reservation.dto.ReservationRequest;
import com.pl.warehouse.reservation.dto.ReservationResponse;
import com.pl.warehouse.warehouse.Warehouse;
import com.pl.warehouse.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: Reserve / release / fulfill — coordinates reservation row + inventory.reserved_quantity.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StockReservationRepository reservationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<ReservationResponse> list(ReservationStatus status) {
        List<StockReservation> rows = status == null
                ? reservationRepository.findAllByOrderByCreatedAtDesc()
                : reservationRepository.findByStatusOrderByCreatedAtDesc(status);
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.warehouseId()));
        WarehouseItem item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", request.itemId()));

        inventoryService.reserve(warehouse.getId(), item.getId(), request.quantity());

        StockReservation reservation = new StockReservation();
        reservation.setWarehouse(warehouse);
        reservation.setItem(item);
        reservation.setQuantity(request.quantity());
        reservation.setReference(request.reference().trim());
        reservation.setNotes(request.notes());
        reservation.setStatus(ReservationStatus.ACTIVE);
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse release(UUID id) {
        StockReservation reservation = find(id);
        requireActive(reservation);
        inventoryService.releaseReservation(
                reservation.getWarehouse().getId(),
                reservation.getItem().getId(),
                reservation.getQuantity()
        );
        reservation.setStatus(ReservationStatus.RELEASED);
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse fulfill(UUID id) {
        StockReservation reservation = find(id);
        requireActive(reservation);
        inventoryService.fulfillReservation(
                reservation.getWarehouse().getId(),
                reservation.getItem().getId(),
                reservation.getQuantity(),
                "Fulfill reservation " + reservation.getReference()
        );
        reservation.setStatus(ReservationStatus.FULFILLED);
        return toResponse(reservationRepository.save(reservation));
    }

    private void requireActive(StockReservation reservation) {
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Reservation is not ACTIVE: " + reservation.getStatus()
            );
        }
    }

    private StockReservation find(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockReservation", id));
    }

    private ReservationResponse toResponse(StockReservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getWarehouse().getId(),
                r.getWarehouse().getName(),
                r.getItem().getId(),
                r.getItem().getItemName(),
                r.getQuantity(),
                r.getReference(),
                r.getStatus().name(),
                r.getNotes(),
                r.getCreatedAt()
        );
    }
}
