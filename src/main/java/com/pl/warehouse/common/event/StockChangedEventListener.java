package com.pl.warehouse.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * STUDY: {@code @TransactionalEventListener(AFTER_COMMIT)} runs only if the DB commit succeeded.
 * If transfer rolls back, this listener never fires — no false “stock changed” logs.
 */
@Component
public class StockChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockChangedEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockChanged(StockChangedEvent event) {
        log.info(
                "STOCK_CHANGED inventory={} warehouse={} item={} delta={} after={} reason={}",
                event.inventoryId(),
                event.warehouseId(),
                event.itemId(),
                event.quantityDelta(),
                event.quantityAfter(),
                event.reason()
        );
    }
}
