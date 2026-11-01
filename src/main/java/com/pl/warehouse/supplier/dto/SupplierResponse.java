package com.pl.warehouse.supplier.dto;

import java.util.UUID;

/**
 * STUDY: Response DTO for supplier — API contract separate from the JPA entity.
 */
public record SupplierResponse(
        UUID id,
        String name,
        String email,
        String phone
) {
}
