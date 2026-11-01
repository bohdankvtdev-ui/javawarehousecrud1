package com.pl.warehouse.item.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemRequest(
        String itemName,
        String type,
        String description,
        UUID categoryId,
        UUID supplierId,
        BigDecimal price,
        BigDecimal weight
) {
}