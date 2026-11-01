/**
 * STUDY — Dashboard (CRM Overview) feature package.
 *
 * <p><b>What lives here:</b> read-only aggregations across warehouses, items, inventory,
 * and stock movements for the home KPIs.
 *
 * <p><b>Responsibility:</b> one summary endpoint so the frontend avoids N separate count calls.
 * No writes — services use {@code @Transactional(readOnly = true)}.
 *
 * <p>Endpoint: {@code GET /api/v1/dashboard/summary}.
 */
package com.pl.warehouse.dashboard;
