-- STUDY: V4 — catalog SKUs (WarehouseItem).
-- Stock quantity is NOT here → see inventory (V5).

CREATE TABLE warehouse_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_name   VARCHAR(150) NOT NULL,
    type        VARCHAR(50),
    description TEXT,

    category_id UUID NOT NULL REFERENCES categories (id),
    supplier_id UUID NOT NULL REFERENCES suppliers (id),

    price  NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    weight NUMERIC(10, 2) CHECK (weight IS NULL OR weight >= 0),

    -- Must match Java enum ItemStatus
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED'))
);

CREATE INDEX idx_warehouse_items_category ON warehouse_items (category_id);
CREATE INDEX idx_warehouse_items_supplier ON warehouse_items (supplier_id);
CREATE INDEX idx_warehouse_items_status   ON warehouse_items (status);

COMMENT ON TABLE warehouse_items IS 'Catalog items (SKU). Quantity lives in inventory.';
