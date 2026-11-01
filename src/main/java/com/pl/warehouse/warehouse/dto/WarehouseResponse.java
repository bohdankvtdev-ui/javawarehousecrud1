package com.pl.warehouse.warehouse.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * STUDY: Response DTO for warehouse — includes {@code createdAt}; not the JPA entity.
 */
public record WarehouseResponse(
        UUID id,
        String name,
        String address,
        String city,
        Integer capacity,
        Instant createdAt
) {
}
