package com.pl.warehouse.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory,UUID> {
    List<Inventory> findByWarehouseId(UUID warehouseId);

    List<Inventory> findByItemId(UUID itemId);

    @Query("""
            SELECT inv
            FROM Inventory inv
            WHERE inv.quantity<inv.minQuantity
            AND inv.warehouse.id = :warehouseId
            """)
    List<Inventory> findLowStockByWarehouse(
            @Param("warehouseId") UUID warehouseId
    );
}
