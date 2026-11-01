/**
 * STUDY (mid-level CRM) — Purchase orders (inbound supply).
 *
 * <p>Header ({@link com.pl.warehouse.purchaseorder.PurchaseOrder}) + lines
 * ({@link com.pl.warehouse.purchaseorder.PurchaseOrderLine}) is a classic 1:N ERP pattern.
 * Receiving lines calls inventory adjust inside one {@code @Transactional} boundary.
 *
 * <p>Statuses: DRAFT → ORDERED → PARTIAL / RECEIVED (or CANCELLED).
 */
package com.pl.warehouse.purchaseorder;
