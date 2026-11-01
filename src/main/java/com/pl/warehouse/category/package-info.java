/**
 * STUDY — Category (lookup) feature package.
 *
 * <p><b>What lives here:</b> Category entity, DTOs, repository, service, controller.
 *
 * <p><b>Responsibility:</b> small reference list used to group catalog items
 * (Electronics, Tools, …). Items hold a FK to category; stock lives in inventory.
 *
 * <p><b>Typical flow:</b> HTTP {@code /api/v1/categories} → CategoryController →
 * CategoryService → CategoryRepository.
 */
package com.pl.warehouse.category;
