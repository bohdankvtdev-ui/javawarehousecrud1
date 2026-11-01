package com.pl.warehouse.purchaseorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * STUDY: Partial receive — each line gets {@code quantity} more goods into the PO warehouse.
 */
public record ReceivePurchaseOrderRequest(
        @NotEmpty @Valid List<ReceiveLine> lines
) {
    public record ReceiveLine(
            @NotNull UUID lineId,
            @NotNull @Min(1) Integer quantity
    ) {
    }
}
