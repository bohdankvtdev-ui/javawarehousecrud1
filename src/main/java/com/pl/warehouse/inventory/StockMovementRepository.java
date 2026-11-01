package com.pl.warehouse.inventory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: Spring Data repository for the stock movement audit trail.
 * Derived {@code OrderByCreatedAtDesc} + {@link Pageable} = recent history without custom SQL.
 */
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByItemIdOrderByCreatedAtDesc(UUID itemId, Pageable pageable);

    List<StockMovement> findByWarehouseIdOrderByCreatedAtDesc(UUID warehouseId, Pageable pageable);

    List<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
