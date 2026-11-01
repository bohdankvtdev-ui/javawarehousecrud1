-- STUDY (mid-level): concurrency + availability.
-- version       → optimistic locking (@Version) — two users editing same stock → 409
-- reserved_qty  → stock held for a customer/order; available = quantity - reserved

ALTER TABLE inventory
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN reserved_quantity INTEGER NOT NULL DEFAULT 0
        CHECK (reserved_quantity >= 0);

ALTER TABLE inventory
    ADD CONSTRAINT chk_inventory_reserved_lte_qty
        CHECK (reserved_quantity <= quantity);

COMMENT ON COLUMN inventory.version IS 'Optimistic lock counter (JPA @Version)';
COMMENT ON COLUMN inventory.reserved_quantity IS 'Qty held; available = quantity - reserved_quantity';
