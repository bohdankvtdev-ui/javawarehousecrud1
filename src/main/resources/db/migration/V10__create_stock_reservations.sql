-- STUDY (mid-level CRM): reserve stock without shipping yet (sales hold / pick wave).
-- Soft allocation: increases inventory.reserved_quantity; release/fulfill reverses or ships.

CREATE TABLE stock_reservations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_id    UUID NOT NULL REFERENCES warehouses (id),
    item_id         UUID NOT NULL REFERENCES warehouse_items (id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    reference       VARCHAR(80) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'RELEASED', 'FULFILLED')),
    notes           VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservations_active ON stock_reservations (warehouse_id, item_id, status);
CREATE INDEX idx_reservations_reference ON stock_reservations (reference);

COMMENT ON TABLE stock_reservations IS 'CRM: hold available stock for a reference (order, pick list)';
