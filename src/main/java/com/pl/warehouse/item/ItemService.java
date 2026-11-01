package com.pl.warehouse.item;

import com.pl.warehouse.category.Category;
import com.pl.warehouse.category.CategoryRepository;
import com.pl.warehouse.common.api.PageResponse;
import com.pl.warehouse.common.exception.ResourceNotFoundException;
import com.pl.warehouse.item.dto.ItemRequest;
import com.pl.warehouse.item.dto.ItemResponse;
import com.pl.warehouse.supplier.Supplier;
import com.pl.warehouse.supplier.SupplierRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * STUDY — Business logic for catalog items (rules + orchestration).
 *
 * <p>Mapping lives in {@link ItemMapper} (SRP). Specs live in {@link ItemSpecs}.
 * This class answers: “May we create/update/delete, and with which defaults?”
 *
 * <p>{@code @Transactional} marks the DB transaction boundary for the whole method.
 * Writes need a transaction so {@code save}/{@code delete} flush safely; reads use
 * {@code readOnly = true} and keep the persistence context open so LAZY
 * category/supplier can load inside {@link ItemMapper#toResponse}.
 *
 * <p>Principles: docs/PRINCIPLES.md (SRP, DIP via constructor injection).
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ItemMapper itemMapper;

    public ItemService(
            ItemRepository itemRepository,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            ItemMapper itemMapper
    ) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.itemMapper = itemMapper;
    }

    /**
     * Dynamic filter search. Pagination keeps large catalogs usable.
     * {@code readOnly = true}: hint for the provider (no dirty checking / flush for reads)
     * and keeps the session open while mapping LAZY associations on the page.
     */
    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> searchItems(
            String q,
            ItemStatus status,
            UUID categoryId,
            Pageable pageable
    ) {
        return PageResponse.from(
                itemRepository
                        .findAll(ItemSpecs.withFilters(q, status, categoryId), pageable)
                        .map(itemMapper::toResponse)
        );
    }

    /**
     * Load one item or fail with a clear not-found signal (→ HTTP 404).
     * {@code readOnly}: same session + LAZY mapping needs as {@link #searchItems}.
     */
    @Transactional(readOnly = true)
    public ItemResponse getItemById(UUID id) {
        return itemMapper.toResponse(findItem(id));
    }

    /**
     * Create catalog item.
     * Invariant: category and supplier must exist; new items start ACTIVE.
     * {@code @Transactional}: one DB unit of work for FK lookups + insert + mapping.
     */
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        Category category = requireCategory(request.categoryId());
        Supplier supplier = requireSupplier(request.supplierId());

        WarehouseItem item = new WarehouseItem();
        itemMapper.applyRequest(item, request, category, supplier);
        item.setStatus(ItemStatus.ACTIVE);

        return itemMapper.toResponse(itemRepository.save(item));
    }

    /**
     * Update existing item; same FK invariants as create.
     * {@code @Transactional}: load + mutate + flush as one commit.
     */
    @Transactional
    public ItemResponse updateItem(UUID id, ItemRequest request) {
        WarehouseItem item = findItem(id);
        itemMapper.applyRequest(
                item,
                request,
                requireCategory(request.categoryId()),
                requireSupplier(request.supplierId())
        );
        return itemMapper.toResponse(itemRepository.save(item));
    }

    /**
     * CRM: change catalog lifecycle without rewriting the whole item
     * (ACTIVE → INACTIVE / DISCONTINUED).
     * {@code @Transactional}: status patch + save in one commit.
     */
    @Transactional
    public ItemResponse updateStatus(UUID id, ItemStatus status) {
        WarehouseItem item = findItem(id);
        item.setStatus(status);
        return itemMapper.toResponse(itemRepository.save(item));
    }

    /**
     * Delete after ensuring the row exists (explicit 404 vs silent no-op).
     * {@code @Transactional}: find + delete as one unit of work.
     */
    @Transactional
    public void deleteItem(UUID id) {
        itemRepository.delete(findItem(id));
    }

    private WarehouseItem findItem(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    private Category requireCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private Supplier requireSupplier(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }
}
