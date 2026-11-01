-- =============================================================================
-- STUDY: Query optimization indexes (see docs/QUERY_OPTIMIZATION.md)
-- =============================================================================
-- BEFORE: ILIKE '%laptop%' → often Seq Scan (B-tree cannot use leading %).
-- AFTER:  pg_trgm GIN indexes → Bitmap Index Scan for contains-search.
-- Also: lower(item_name) B-tree for prefix / sort-friendly lookups.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Contains search (UI ?q=) — supports LIKE/ILIKE '%text%'
CREATE INDEX IF NOT EXISTS idx_items_name_trgm
    ON warehouse_items USING GIN (item_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_items_type_trgm
    ON warehouse_items USING GIN (type gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_items_description_trgm
    ON warehouse_items USING GIN (description gin_trgm_ops);

-- Prefix / equality-friendly (LIKE 'Think%' or ORDER BY lower(item_name))
CREATE INDEX IF NOT EXISTS idx_items_name_lower
    ON warehouse_items (lower(item_name));

-- Composite: status filter + category (common Items page combo)
CREATE INDEX IF NOT EXISTS idx_items_status_category
    ON warehouse_items (status, category_id);

COMMENT ON INDEX idx_items_name_trgm IS 'STUDY: trigram GIN — makes ?q= contains-search scale';
COMMENT ON INDEX idx_items_name_lower IS 'STUDY: B-tree on lower(name) — prefix search / sort';
