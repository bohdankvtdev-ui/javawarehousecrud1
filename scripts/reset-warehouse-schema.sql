-- =============================================================================
-- STUDY: Reset ONLY warehouse tables + Flyway history, then restart the app.
-- Keeps unrelated tables (e.g. player_data / player_stats) untouched.
--
-- How to run:
--   psql -h localhost -U postgres -d prem_stats -f scripts/reset-warehouse-schema.sql
--
-- Then start Spring Boot again → Flyway re-applies V1…V13.
-- =============================================================================

DROP TABLE IF EXISTS stock_reservations CASCADE;
DROP TABLE IF EXISTS purchase_order_lines CASCADE;
DROP TABLE IF EXISTS purchase_orders CASCADE;
DROP TABLE IF EXISTS stock_movements CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS warehouse_items CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS warehouses CASCADE;

-- Forget old migration checksums / versions for this schema
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
