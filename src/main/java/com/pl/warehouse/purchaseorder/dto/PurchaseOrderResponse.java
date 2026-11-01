package com.pl.warehouse.purchaseorder.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
        UUID id,
        String poNumber,
        UUID supplierId,
        String supplierName,
        UUID warehouseId,
        String warehouseName,
        String status,
        Instant orderedAt,
        LocalDate expectedAt,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        List<PurchaseOrderLineResponse> lines
) {
}
