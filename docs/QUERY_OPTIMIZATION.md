# Query optimization — before / after (study)

Goal: see **why** indexes, pagination, and fetch strategies matter when the catalog grows (this repo seeds ~70+ items and many stock rows).

Run examples against Postgres after Flyway V12–V13. Turn on timing:

```sql
\timing on
EXPLAIN (ANALYZE, BUFFERS)
...
```

---

## 1. Catalog search `?q=laptop`

### Before (no useful index for `%laptop%`)

```sql
-- Leading wildcard → B-tree on item_name cannot be used
SELECT * FROM warehouse_items
WHERE lower(item_name) LIKE '%laptop%'
   OR lower(type) LIKE '%laptop%'
   OR lower(description) LIKE '%laptop%';
```

Typical plan: **Seq Scan** on `warehouse_items` (read every row). Fine for 12 rows; painful at 100k+.

### After (V13: `pg_trgm` GIN)

```sql
-- Same query, but GIN trigram indexes support LIKE/ILIKE '%…%'
SELECT * FROM warehouse_items
WHERE item_name ILIKE '%laptop%'
   OR type ILIKE '%laptop%'
   OR description ILIKE '%laptop%';
```

Typical plan: **Bitmap Index Scan** on `idx_items_name_trgm` (and friends).

| | Before | After |
|-|--------|-------|
| Plan | Seq Scan | Bitmap / Index Scan |
| Scales with | table size | match selectivity |
| Why better | — | Trigrams index substrings; B-tree does not help leading `%` |

**Even better for typeahead:** prefix search `LIKE 'lap%'` can use a plain B-tree on `lower(item_name)` (`idx_items_name_lower`). Our API still uses contains-search for UX; indexes make contains affordable.

Java side: `ItemSpecs.textSearch` — see comments in that class.

---

## 2. Filter by category (avoid useless join)

### Before

```java
root.join("category", JoinType.INNER);
cb.equal(category.get("id"), categoryId);
```

May emit an extra JOIN when the FK `category_id` is already on `warehouse_items`.

### After (this project)

```java
cb.equal(root.get("category").get("id"), categoryId);
```

Hibernate can predicate on `category_id` directly. Index `idx_warehouse_items_category` (V4) supports it.

| | Before | After |
|-|--------|-------|
| SQL shape | JOIN categories | WHERE category_id = ? |
| Why better | Extra join work | Uses FK + existing index |

---

## 3. N+1 when mapping DTOs (items list)

### Before

```text
SELECT * FROM warehouse_items ... LIMIT 10
-- then for EACH row:
SELECT * FROM categories WHERE id = ?
SELECT * FROM suppliers WHERE id = ?
→ 1 + 10 + 10 queries
```

### After (`@EntityGraph` on `ItemRepository.findAll(Specification, Pageable)`)

```text
SELECT wi.*, c.*, s.*
FROM warehouse_items wi
JOIN categories c ...
JOIN suppliers s ...
→ ~1–2 queries for the page
```

| | Before | After |
|-|--------|-------|
| Queries | O(n) | O(1) per page |
| Why better | Less round-trips / latency | Fetch graph loads associations once |

Same idea: `InventoryRepository` EntityGraphs for warehouse + item.

---

## 4. Pagination vs `findAll()`

### Before

```java
itemRepository.findAll(); // loads entire catalog into memory
```

### After

```java
itemRepository.findAll(spec, PageRequest.of(page, size, sort));
// + PageResponse with totalElements
```

| | Before | After |
|-|--------|-------|
| Memory | grows with table | bounded by page size |
| Network | huge JSON | small page |
| Why better | — | Scales; UI already does this on Items |

---

## 5. Low-stock query

Index `idx_inventory_low_stock (warehouse_id, quantity, min_quantity)` (V5) helps:

```sql
SELECT * FROM inventory
WHERE warehouse_id = :id AND quantity < min_quantity;
```

**Why:** filter starts with equality on `warehouse_id` (selective), then compares qty columns. Without the composite index Postgres may still seq-scan a large inventory table.

---

## 6. Optimistic lock `version` (not a search index)

`inventory.version` starts at **0** for every new/seeded row. It increments **only on UPDATE** (adjust, transfer, upsert, receive…). Seeing `0` everywhere means those rows were never updated since insert — expected, not a bug.

After one Adjust: that row becomes `1`, then `2`, …

---

## How to practice

1. Seed V12 applied → many items  
2. `EXPLAIN ANALYZE` the LIKE query before/after dropping `idx_items_name_trgm` (study only — recreate after)  
3. Watch SQL log (`spring.jpa.show-sql=true`) while opening Items page — should not spam category selects  
4. Read `ItemSpecs` + `ItemRepository` STUDY comments  

Related: [BACKEND_STUDY.md](BACKEND_STUDY.md), [MID_LEVEL.md](MID_LEVEL.md), [DATABASE.md](DATABASE.md).
