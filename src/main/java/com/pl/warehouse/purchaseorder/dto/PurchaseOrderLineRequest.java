package com.pl.warehouse.purchaseorder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/** STUDY: One line when creating a PO. */
public record PurchaseOrderLineRequest(
        @NotNull UUID itemId,
        @NotNull @Min(1) Integer quantityOrdered,
        BigDecimal unitCost
) {
}
