/**
 * STUDY — Warehouse (location) feature package.
 *
 * <p><b>What lives here:</b> Warehouse entity, DTOs, repository, service, controller.
 *
 * <p><b>Responsibility:</b> physical locations. Inventory rows are unique per
 * (warehouse, item). Transfers move quantity between warehouses.
 *
 * <p><b>Typical flow:</b> HTTP {@code /api/v1/warehouses} → WarehouseController →
 * WarehouseService → WarehouseRepository.
 */
package com.pl.warehouse.warehouse;
