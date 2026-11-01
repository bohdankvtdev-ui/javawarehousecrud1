package com.pl.warehouse.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * STUDY: Request DTO for relative stock change.
 * {@code delta} &gt; 0 receive goods, {@code delta} &lt; 0 ship/consume.
 * Absolute SET is done via upsert; this is relative change only.
 */
public record StockAdjustRequest(
        @NotNull Integer delta,
        @Size(max = 255) String reason
) {
}
