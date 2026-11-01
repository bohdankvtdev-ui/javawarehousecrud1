-- STUDY: V2 — item categories (Electronics, Tools, …)

CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

COMMENT ON TABLE categories IS 'Lookup table for grouping warehouse items';
