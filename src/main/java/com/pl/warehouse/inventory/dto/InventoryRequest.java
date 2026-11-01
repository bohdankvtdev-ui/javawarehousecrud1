package com.pl.warehouse.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * STUDY: Request DTO for create / upsert / update stock.
 * {@code @NotNull}/{@code @Min} run when the controller uses {@code @Valid}.
 * Identifies stock by warehouseId + itemId (matches the unique DB constraint).
 */
public record InventoryRequest(
        @NotNull UUID warehouseId,
        @NotNull UUID itemId,
        @NotNull @Min(0) Integer quantity,
        @NotNull @Min(0) Integer minQuantity,
        @Size(max = 50) String location,
        @Size(max = 20) String warehouseSection
) {
}
