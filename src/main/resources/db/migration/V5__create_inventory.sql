CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    item_id UUID NOT NULL REFERENCES warehouse_items(id),

    quantity INTEGER NOT NULL DEFAULT 0,
    min_quantity INTEGER NOT NULL DEFAULT 0,

    location VARCHAR(50),
    warehouse_section VARCHAR(20),

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (warehouse_id, item_id),

    CHECK (quantity >= 0),
    CHECK (min_quantity >= 0)
);

