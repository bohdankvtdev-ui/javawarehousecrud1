-- =============================================================================
-- STUDY: Flyway migration V1 — warehouses
-- =============================================================================
-- File name: V{number}__{description}.sql  (two underscores)
-- Flyway runs each version ONCE, in order. After it runs on a DB, do not edit
-- this file casually — either add V7, V8… or reset the schema (see DATABASE.md).
-- =============================================================================

-- Needed for gen_random_uuid() on older Postgres; harmless if already present.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE warehouses (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(150) NOT NULL UNIQUE,
    address    VARCHAR(255),
    city       VARCHAR(100),
    capacity   INTEGER CHECK (capacity IS NULL OR capacity >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE warehouses IS 'Physical warehouse locations';
