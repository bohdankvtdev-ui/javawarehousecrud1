package com.pl.warehouse.item;

/**
 * STUDY: Catalog lifecycle for a {@link WarehouseItem}.
 * Stored via {@code @Enumerated(STRING)} so renaming ordinals never corrupts rows.
 * ACTIVE = sellable; INACTIVE = hidden from normal ops; DISCONTINUED = retired SKU.
 */
public enum ItemStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED
}
