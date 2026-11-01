-- STUDY: Audit trail for warehouse CRM operations (receive / ship / transfer / set).
-- Advanced apps always log WHY stock changed — not only the final quantity.

CREATE TABLE stock_movements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id    UUID REFERENCES inventory (id) ON DELETE SET NULL,
    warehouse_id    UUID NOT NULL REFERENCES warehouses (id),
    item_id         UUID NOT NULL REFERENCES warehouse_items (id),
    movement_type   VARCHAR(30) NOT NULL
        CHECK (movement_type IN ('RECEIVE', 'SHIP', 'ADJUST', 'TRANSFER_OUT', 'TRANSFER_IN', 'SET')),
    quantity_delta  INTEGER NOT NULL,
    quantity_after  INTEGER NOT NULL CHECK (quantity_after >= 0),
    reason          VARCHAR(255),
    related_warehouse_id UUID REFERENCES warehouses (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_movements_item ON stock_movements (item_id, created_at DESC);
CREATE INDEX idx_stock_movements_warehouse ON stock_movements (warehouse_id, created_at DESC);

COMMENT ON TABLE stock_movements IS 'CRM audit log of stock quantity changes';
