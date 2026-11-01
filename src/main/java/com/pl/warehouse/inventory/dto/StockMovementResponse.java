package com.pl.warehouse.inventory.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * STUDY: Response DTO for audit history — flattened names + movement type as String.
 * {@code relatedWarehouse*} is set for transfers (the other warehouse in the move).
 */
public record StockMovementResponse(
        UUID id,
        UUID warehouseId,
        String warehouseName,
        UUID itemId,
        String itemName,
        String movementType,
        int quantityDelta,
        int quantityAfter,
        String reason,
        UUID relatedWarehouseId,
        String relatedWarehouseName,
        Instant createdAt
) {
}
