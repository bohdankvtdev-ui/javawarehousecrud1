package com.pl.warehouse.item;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * STUDY — Dynamic query pieces for {@link ItemRepository}.
 *
 * <p>Each method returns a {@link Specification} = Criteria predicates.
 * Blank inputs → {@code conjunction()} (always true) so filters are optional.
 *
 * <h2>Optimization notes (see docs/QUERY_OPTIMIZATION.md)</h2>
 * <ul>
 *   <li>{@link #textSearch} uses {@code LIKE %q%} (contains). Without {@code pg_trgm}
 *       this is a seq scan; V13 GIN trigram indexes make it scale.</li>
 *   <li>{@link #hasCategory} predicates on the FK path — no extra join needed.</li>
 *   <li>Always pair with {@code Pageable} — never load the full catalog.</li>
 * </ul>
 */
public final class ItemSpecs {

    private ItemSpecs() {
    }

    /**
     * Free-text contains search across name, type, description (case-insensitive).
     *
     * <p><b>Before:</b> seq scan on small tables — OK for 12 rows, bad at 100k.<br>
     * <b>After (V13):</b> {@code gin_trgm_ops} indexes support {@code ILIKE '%q%'}.
     *
     * <p>Stricter alternative for typeahead: prefix {@code q + "%"} (B-tree friendly).
     */
    public static Specification<WarehouseItem> textSearch(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("itemName")), pattern),
                    cb.like(cb.lower(root.get("type")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /** Filter by enum status — uses index {@code idx_warehouse_items_status} / composite V13. */
    public static Specification<WarehouseItem> hasStatus(ItemStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * Filter by category UUID via FK path (not {@code join}).
     *
     * <p><b>Before:</b> {@code root.join("category")} → unnecessary JOIN.<br>
     * <b>After:</b> {@code root.get("category").get("id")} → {@code WHERE category_id = ?}
     * using {@code idx_warehouse_items_category}.
     */
    public static Specification<WarehouseItem> hasCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /** AND all optional filters for {@code GET /api/v1/items}. */
    public static Specification<WarehouseItem> withFilters(String q, ItemStatus status, UUID categoryId) {
        return Specification.allOf(
                textSearch(q),
                hasStatus(status),
                hasCategory(categoryId)
        );
    }
}
