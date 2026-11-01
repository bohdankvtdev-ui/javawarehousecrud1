-- STUDY (mid-level CRM): Purchase orders = “we ordered stock from a supplier”.
-- Header + lines is a classic 1:N design used in ERPs / warehouse systems.
-- Receiving a PO increases inventory inside one @Transactional service method.

CREATE TABLE purchase_orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    po_number       VARCHAR(40) NOT NULL,
    supplier_id     UUID NOT NULL REFERENCES suppliers (id),
    warehouse_id    UUID NOT NULL REFERENCES warehouses (id),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'ORDERED', 'PARTIAL', 'RECEIVED', 'CANCELLED')),
    ordered_at      TIMESTAMPTZ,
    expected_at     DATE,
    notes           VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_purchase_orders_po_number UNIQUE (po_number)
);

CREATE INDEX idx_po_supplier ON purchase_orders (supplier_id);
CREATE INDEX idx_po_warehouse ON purchase_orders (warehouse_id);
CREATE INDEX idx_po_status ON purchase_orders (status);

CREATE TABLE purchase_order_lines (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id  UUID NOT NULL REFERENCES purchase_orders (id) ON DELETE CASCADE,
    item_id            UUID NOT NULL REFERENCES warehouse_items (id),
    quantity_ordered   INTEGER NOT NULL CHECK (quantity_ordered > 0),
    quantity_received  INTEGER NOT NULL DEFAULT 0 CHECK (quantity_received >= 0),
    unit_cost          NUMERIC(12, 2),
    CONSTRAINT uq_po_line_item UNIQUE (purchase_order_id, item_id),
    CONSTRAINT chk_po_line_received_lte_ordered CHECK (quantity_received <= quantity_ordered)
);

CREATE INDEX idx_po_lines_po ON purchase_order_lines (purchase_order_id);

COMMENT ON TABLE purchase_orders IS 'CRM: inbound orders from suppliers into a warehouse';
COMMENT ON TABLE purchase_order_lines IS 'Line items on a purchase order; receive qty drives stock.';
