package com.pl.warehouse.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * STUDY: Request DTO for warehouse-to-warehouse transfer of one item.
 * Validated with {@code @Valid}; service enforces from ≠ to and sufficient source qty.
 */
public record StockTransferRequest(
        @NotNull UUID fromWarehouseId,
        @NotNull UUID toWarehouseId,
        @NotNull UUID itemId,
        @NotNull @Min(1) Integer quantity,
        @Size(max = 255) String reason
) {
}
