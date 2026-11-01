/**
 * STUDY — Supplier feature package.
 *
 * <p><b>What lives here:</b> Supplier entity, DTOs, repository, service, controller.
 *
 * <p><b>Responsibility:</b> vendors that supply catalog items. {@code WarehouseItem}
 * references a supplier; reorder suggestions surface the supplier name for purchasing.
 *
 * <p><b>Typical flow:</b> HTTP {@code /api/v1/suppliers} → SupplierController →
 * SupplierService → SupplierRepository.
 */
package com.pl.warehouse.supplier;
