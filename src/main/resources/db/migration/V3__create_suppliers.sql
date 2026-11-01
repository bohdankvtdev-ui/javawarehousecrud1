-- STUDY: V3 — suppliers you buy catalog items from

CREATE TABLE suppliers (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name  VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(30)
);

COMMENT ON TABLE suppliers IS 'Vendors that supply warehouse items';
