package com.pl.warehouse.inventory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * STUDY: Stock repository — derived queries + JPQL for low-stock.
 *
 * <p>{@code @EntityGraph} avoids N+1 when listing (warehouse name + item name).
 * Low-stock uses index {@code idx_inventory_low_stock} (V5) — see QUERY_OPTIMIZATION.md.
 */
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    @Override
    @EntityGraph(attributePaths = {"warehouse", "item", "item.supplier"})
    List<Inventory> findAll();

    @EntityGraph(attributePaths = {"warehouse", "item"})
    List<Inventory> findByWarehouseId(UUID warehouseId);

    List<Inventory> findByItemId(UUID itemId);

    /**
     * STUDY: backs unique (warehouse_id, item_id) — upsert / transfer / reserve lookups.
     */
    Optional<Inventory> findByWarehouseIdAndItemId(UUID warehouseId, UUID itemId);

    @EntityGraph(attributePaths = {"warehouse", "item", "item.supplier"})
    @Query("""
            SELECT inv
            FROM Inventory inv
            WHERE inv.quantity < inv.minQuantity
              AND inv.warehouse.id = :warehouseId
            """)
    List<Inventory> findLowStockByWarehouse(@Param("warehouseId") UUID warehouseId);
}
