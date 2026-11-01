package com.pl.warehouse.inventory;

/**
 * STUDY: Why did stock change? Stored as STRING via {@code @Enumerated} on {@link StockMovement}.
 * RECEIVE = goods in, SHIP = goods out, ADJUST = manual correction,
 * TRANSFER_* = between warehouses, SET = absolute upsert/replace quantity.
 */
public enum StockMovementType {
    RECEIVE,
    SHIP,
    ADJUST,
    TRANSFER_OUT,
    TRANSFER_IN,
    SET
}
