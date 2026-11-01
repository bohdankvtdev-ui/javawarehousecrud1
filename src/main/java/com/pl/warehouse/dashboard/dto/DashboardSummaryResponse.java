package com.pl.warehouse.dashboard.dto;

import java.math.BigDecimal;

/**
 * STUDY: Response DTO for CRM home metrics — one round-trip for the Overview page.
 * Counts + inventory value; not an entity (assembled in {@link com.pl.warehouse.dashboard.DashboardService}).
 */
public record DashboardSummaryResponse(
        long warehouseCount,
        long categoryCount,
        long supplierCount,
        long itemCount,
        long activeItemCount,
        long inventoryRowCount,
        long lowStockCount,
        BigDecimal inventoryValue,
        long movementCount
) {
}
