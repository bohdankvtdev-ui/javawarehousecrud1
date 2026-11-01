package com.pl.warehouse.purchaseorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** STUDY: Create PO in DRAFT (or submit as ORDERED via service flag). */
public record PurchaseOrderRequest(
        @NotBlank @Size(max = 40) String poNumber,
        @NotNull UUID supplierId,
        @NotNull UUID warehouseId,
        LocalDate expectedAt,
        @Size(max = 500) String notes,
        @NotEmpty @Valid List<PurchaseOrderLineRequest> lines
) {
}
