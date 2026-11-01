package com.pl.warehouse.purchaseorder.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderLineResponse(
        UUID id,
        UUID itemId,
        String itemName,
        Integer quantityOrdered,
        Integer quantityReceived,
        Integer remaining,
        BigDecimal unitCost
) {
}
