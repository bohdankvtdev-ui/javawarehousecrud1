package com.pl.warehouse.inventory.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * STUDY: Stock DTO — includes optimistic-lock {@code version} and availability
 * ({@code available = quantity - reservedQuantity}).
 */
public record InventoryResponse(
        UUID id,
        UUID warehouseId,
        String warehouseName,
        UUID itemId,
        String itemName,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        Integer minQuantity,
        Long version,
        String location,
        String warehouseSection,
        Instant updatedAt,
        boolean lowStock
) {
}
