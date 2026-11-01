package com.pl.warehouse.common.event;

import java.util.UUID;

/**
 * STUDY (mid-level): Domain event published after stock changes.
 * Decouples “stock changed” from side effects (logging, email, metrics, Kafka later).
 * Listeners run in the same thread by default after the publisher returns —
 * for async use {@code @Async} + {@code @EnableAsync}.
 */
public record StockChangedEvent(
        UUID inventoryId,
        UUID warehouseId,
        UUID itemId,
        int quantityDelta,
        int quantityAfter,
        String reason
) {
}
