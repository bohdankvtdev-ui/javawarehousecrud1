package com.pl.warehouse.item;

import com.pl.warehouse.category.Category;
import com.pl.warehouse.item.dto.ItemRequest;
import com.pl.warehouse.item.dto.ItemResponse;
import com.pl.warehouse.supplier.Supplier;
import org.springframework.stereotype.Component;

/**
 * STUDY — Single Responsibility: mapping only.
 *
 * <p>Why extract this from {@link ItemService}?
 * <ul>
 *   <li>Service stays focused on rules (FK checks, status defaults, transactions)</li>
 *   <li>Mapping often grows (more fields, nested DTOs) — change it here without touching rules</li>
 *   <li>Easier to unit-test mapping edge cases in isolation later</li>
 * </ul>
 *
 * <p>See docs/PRINCIPLES.md → SOLID → Single Responsibility.
 *
 * <p>Alternative: MapStruct (generated mappers) in larger codebases.
 */
@Component
public class ItemMapper {

    /**
     * Entity → API response. Flattens category/supplier for a simple React table.
     * Requires category & supplier to be initialized (call inside {@code @Transactional}).
     */
    public ItemResponse toResponse(WarehouseItem item) {
        return new ItemResponse(
                item.getId(),
                item.getItemName(),
                item.getType(),
                item.getDescription(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getSupplier().getId(),
                item.getSupplier().getName(),
                item.getPrice(),
                item.getWeight(),
                item.getStatus().name()
        );
    }

    /**
     * Copies writable fields from the request onto the entity.
     * Does not set id/status — those are controlled by the service.
     */
    public void applyRequest(
            WarehouseItem item,
            ItemRequest request,
            Category category,
            Supplier supplier
    ) {
        item.setItemName(request.itemName());
        item.setType(request.type());
        item.setDescription(request.description());
        item.setCategory(category);
        item.setSupplier(supplier);
        item.setPrice(request.price());
        item.setWeight(request.weight());
    }
}
