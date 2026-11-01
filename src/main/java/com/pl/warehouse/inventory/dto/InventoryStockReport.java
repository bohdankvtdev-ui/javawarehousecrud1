package com.pl.warehouse.inventory.dto;

import java.util.UUID;

/**
 * STUDY: Flat report row produced by JdbcTemplate (not a JPA entity).
 * Same idea as other response DTOs — stable JSON shape for the UI / exports.
 */
public record InventoryStockReport(
        UUID inventoryId,
        String warehouseName,
        String itemName,
        int quantity,
        int minQuantity,
        boolean lowStock
) {
}
