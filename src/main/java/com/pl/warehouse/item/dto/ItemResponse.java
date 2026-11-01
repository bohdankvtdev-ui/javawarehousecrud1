package com.pl.warehouse.item.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * STUDY: Response DTO — outbound JSON contract for the frontend.
 * Flattened category/supplier names so the UI does not need nested objects or extra calls.
 * Prefer DTOs over entities so persistence / LAZY proxies never leak over HTTP.
 */
public record ItemResponse(
        UUID id,
        String itemName,
        String type,
        String description,
        UUID categoryId,
        String categoryName,
        UUID supplierId,
        String supplierName,
        BigDecimal price,
        BigDecimal weight,
        String status
) {
}
