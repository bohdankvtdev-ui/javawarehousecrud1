package com.pl.warehouse.item;

import com.pl.warehouse.common.api.PageResponse;
import com.pl.warehouse.item.dto.ItemRequest;
import com.pl.warehouse.item.dto.ItemResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * STUDY — Thin HTTP adapter for warehouse catalog items.
 *
 * <p>{@code @RestController} registers methods as JSON endpoints. Stay thin:
 * parse input → call {@link ItemService} → return DTOs. No SQL / business rules here.
 *
 * <p>{@code @Valid} on request bodies runs Bean Validation before the method body
 * (blank name → 400). {@code @ResponseStatus} sets CREATED (201) / NO_CONTENT (204).
 *
 * <pre>
 * GET    /api/v1/items?q=&status=&categoryId=&page=&size=&sort=  → searchItems
 * GET    /api/v1/items/{id}                                      → getItemById
 * POST   /api/v1/items                                           → createItem   (USER/ADMIN)
 * PUT    /api/v1/items/{id}                                      → updateItem   (USER/ADMIN)
 * PATCH  /api/v1/items/{id}/status?status=                       → updateStatus (USER/ADMIN)
 * DELETE /api/v1/items/{id}                                      → deleteItem   (ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    /**
     * Constructor injection (preferred over {@code @Autowired} on fields).
     * Spring creates one ItemService bean and passes it here.
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Search + filter + paginate catalog items.
     *
     * <p>{@code q} — free text (name/type/description)<br>
     * {@code status} — ACTIVE / INACTIVE / DISCONTINUED<br>
     * {@code categoryId} — UUID filter<br>
     * {@code pageable} — Spring builds this from {@code page}, {@code size}, {@code sort}
     *
     * <p>Example: {@code GET /api/v1/items?q=laptop&page=0&size=10&sort=itemName,asc}
     */
    @GetMapping
    public PageResponse<ItemResponse> searchItems(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) UUID categoryId,
            @PageableDefault(size = 10, sort = "itemName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return itemService.searchItems(q, status, categoryId, pageable);
    }

    /**
     * Fetch one item by primary key. Missing id → 404 via GlobalExceptionHandler.
     */
    @GetMapping("/{id}")
    public ItemResponse getItemById(@PathVariable UUID id) {
        return itemService.getItemById(id);
    }

    /**
     * Create catalog item. {@code @Valid} runs Bean Validation on {@link ItemRequest}
     * before the method body (blank name → 400).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse createItem(@Valid @RequestBody ItemRequest request) {
        return itemService.createItem(request);
    }

    /**
     * Full replace update of an existing item (PUT semantics).
     */
    @PutMapping("/{id}")
    public ItemResponse updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody ItemRequest request
    ) {
        return itemService.updateItem(id, request);
    }

    /**
     * CRM: patch only status — e.g. discontinue without editing price/name.
     * Example: PATCH /api/v1/items/{id}/status?status=DISCONTINUED
     */
    @PatchMapping("/{id}/status")
    public ItemResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam ItemStatus status
    ) {
        return itemService.updateStatus(id, status);
    }

    /**
     * Delete catalog item. Secured as ADMIN in SecurityConfig.
     * Returns HTTP 204 No Content (empty body).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
    }
}
