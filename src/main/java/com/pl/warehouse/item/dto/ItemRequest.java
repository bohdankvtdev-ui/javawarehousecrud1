package com.pl.warehouse.item.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * STUDY: Request DTO — inbound JSON for create/update.
 * Bean Validation annotations ({@code @NotBlank}, {@code @NotNull}, …) run when the
 * controller parameter is marked {@code @Valid}. Never bind the JPA entity directly
 * to the API (hides internals, enables validation, keeps the contract stable).
 */
public record ItemRequest(
        @NotBlank @Size(max = 150) String itemName,
        @Size(max = 50) String type,
        String description,
        @NotNull UUID categoryId,
        @NotNull UUID supplierId,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @DecimalMin("0.0") BigDecimal weight
) {
}
