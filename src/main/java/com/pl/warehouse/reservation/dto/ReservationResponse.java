package com.pl.warehouse.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID warehouseId,
        String warehouseName,
        UUID itemId,
        String itemName,
        Integer quantity,
        String reference,
        String status,
        String notes,
        Instant createdAt
) {
}
