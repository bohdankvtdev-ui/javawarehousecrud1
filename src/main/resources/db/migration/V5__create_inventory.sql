-- STUDY: V5 — stock of one item inside one warehouse.
-- UNIQUE (warehouse_id, item_id) = at most one stock row per pair.

CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    warehouse_id UUID NOT NULL REFERENCES warehouses (id),
    item_id      UUID NOT NULL REFERENCES warehouse_items (id),

    quantity     INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    min_quantity INTEGER NOT NULL DEFAULT 0 CHECK (min_quantity >= 0),

    location          VARCHAR(50),
    warehouse_section VARCHAR(20),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_inventory_warehouse_item UNIQUE (warehouse_id, item_id)
);

CREATE INDEX idx_inventory_warehouse ON inventory (warehouse_id);
CREATE INDEX idx_inventory_item      ON inventory (item_id);
-- Helps low-stock queries: quantity < min_quantity
CREATE INDEX idx_inventory_low_stock ON inventory (warehouse_id, quantity, min_quantity);

COMMENT ON TABLE inventory IS 'Stock levels: which item, which warehouse, how many.';
