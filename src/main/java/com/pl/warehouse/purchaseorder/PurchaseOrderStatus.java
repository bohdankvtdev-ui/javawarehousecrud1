package com.pl.warehouse.purchaseorder;

/**
 * STUDY: PO lifecycle. DRAFT = editable; ORDERED = sent to supplier;
 * PARTIAL/RECEIVED = goods arrived (stock increased); CANCELLED = dead.
 */
public enum PurchaseOrderStatus {
    DRAFT,
    ORDERED,
    PARTIAL,
    RECEIVED,
    CANCELLED
}
