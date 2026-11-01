package com.pl.warehouse.inventory.dto;

import java.util.UUID;

/**
 * STUDY: Response DTO for CRM purchasing — qty &lt; minQuantity, with shortage and supplier.
 * Not an entity; built in the service from Inventory + LAZY associations.
 */
public record ReorderSuggestionResponse(
        UUID inventoryId,
        UUID warehouseId,
        String warehouseName,
        UUID itemId,
        String itemName,
        int quantity,
        int minQuantity,
        int shortage,
        String supplierName
) {
}
