/**
 * STUDY — Inventory (stock) feature package.
 *
 * <p><b>What lives here:</b> {@link com.pl.warehouse.inventory.Inventory} (qty per warehouse+item),
 * {@link com.pl.warehouse.inventory.StockMovement} audit trail, CRM operations
 * (adjust / transfer / reorder), and report DTOs.
 *
 * <p><b>Two data-access styles for learning:</b>
 * <ul>
 *   <li>JPA via {@link com.pl.warehouse.inventory.InventoryService} — CRUD, adjust, transfer, history</li>
 *   <li>JdbcTemplate via {@link com.pl.warehouse.inventory.jdbc.InventoryReportJdbcRepository} — SQL report</li>
 * </ul>
 *
 * <p><b>Transactions matter here:</b> transfer updates two inventory rows and inserts two
 * movements — they must commit or roll back together ({@code @Transactional} on the service method).
 *
 * <p>Endpoint prefix: {@code /api/v1/inventory}.
 */
package com.pl.warehouse.inventory;
