/**
 * STUDY — Item (catalog) feature package.
 *
 * <p><b>What lives here:</b> {@link com.pl.warehouse.item.WarehouseItem} entity,
 * request/response DTOs, {@link com.pl.warehouse.item.ItemRepository},
 * {@link com.pl.warehouse.item.ItemSpecs}, {@link com.pl.warehouse.item.ItemMapper},
 * {@link com.pl.warehouse.item.ItemService}, {@link com.pl.warehouse.item.ItemController}.
 *
 * <p><b>Responsibility:</b> manage catalog SKUs (name, price, category, supplier, status).
 * Stock quantities are NOT here — see {@link com.pl.warehouse.inventory}.
 *
 * <p><b>Layering:</b> Controller (HTTP) → Service (rules + {@code @Transactional}) →
 * Repository (Spring Data JPA). Entities stay internal; the API exposes DTOs only.
 *
 * <p><b>Typical flow:</b> HTTP {@code /api/v1/items} → ItemController → ItemService → ItemRepository.
 *
 * <p>This package is a candidate for a future {@code item-service} microservice.
 */
package com.pl.warehouse.item;
