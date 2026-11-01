# Database & Flyway — how to use it

Simple guide for this study project.

---

## What you have

```text
application.properties
        │
        ▼
   PostgreSQL database (default: prem_stats)
        │
        ▼
   Flyway reads:  src/main/resources/db/migration/
        │
        ├── V1  create warehouses
        ├── V2  create categories
        ├── V3  create suppliers
        ├── V4  create warehouse_items   ← catalog
        ├── V5  create inventory         ← stock
        ├── V6  insert seed data         ← demo rows (fixed UUIDs)
        ├── V7  create stock_movements   ← CRM audit (receive/ship/transfer)
        ├── V8  purchase_orders + lines  ← inbound CRM
        ├── V9  inventory.version/reserved ← optimistic lock + availability
        ├── V10 stock_reservations       ← soft allocation
        ├── V11 seed purchase orders
        ├── V12 expand seed ~6×          ← search/pagination demos
        └── V13 query indexes (pg_trgm)  ← see QUERY_OPTIMIZATION.md
```

On every app start, Flyway:

1. Looks at table `flyway_schema_history`
2. Runs **only new** migrations that are not listed there
3. Then Hibernate `ddl-auto=validate` checks Java entities match the tables

---

## Is the setup correct?

Yes, for a study template:

| Piece | Status |
|-------|--------|
| Parent tables before children | Correct (warehouse/category/supplier → items → inventory) |
| Catalog vs stock split | Correct (`warehouse_items` ≠ quantity; `inventory` = quantity) |
| UUID primary keys | Correct (matches JPA `GenerationType.UUID`) |
| Seed with fixed UUIDs | Correct and easier to study than JOIN/CTE generation |
| Indexes on FKs | Added for common lookups / low-stock |

**Simpler than before:** V6 no longer uses `VALUES … JOIN` + `CROSS JOIN` CTE.  
It uses plain `INSERT … VALUES` with fixed IDs you can copy into curl.

---

## How to use (day to day)

### 1. Start Postgres

Local install, or:

```bash
docker compose up -d
```

### 2. Start the app

```bash
./mvnw spring-boot:run
```

Flyway migrates automatically. You do **not** run the SQL files by hand.

### 3. Check seed data

```bash
# list items
curl -s http://localhost:8080/api/v1/items | head

# known seed item id (ThinkPad)
curl -s http://localhost:8080/api/v1/items/d0000000-0000-4000-8000-000000000001

# low stock in Central Warehouse
curl -s http://localhost:8080/api/v1/inventory/low-stock/a0000000-0000-4000-8000-000000000001
```

### Useful seed IDs

| What | UUID |
|------|------|
| Central Warehouse | `a0000000-0000-4000-8000-000000000001` |
| Northern Warehouse | `a0000000-0000-4000-8000-000000000002` |
| Category Electronics | `b0000000-0000-4000-8000-000000000001` |
| Supplier TechSource | `c0000000-0000-4000-8000-000000000001` |
| Item ThinkPad E14 | `d0000000-0000-4000-8000-000000000001` |

Create a new item using seed foreign keys:

```bash
curl -s -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "USB-C Cable",
    "type": "Cable",
    "description": "Study create example",
    "categoryId": "b0000000-0000-4000-8000-000000000002",
    "supplierId": "c0000000-0000-4000-8000-000000000001",
    "price": 7.50,
    "weight": 0.1
  }'
```

---

## When you change V1–V11 files

Flyway stores a **checksum** of each file. If you edit an already-applied migration, startup fails with a checksum error.

**Study-friendly fix — reset warehouse schema** (keeps `player_*` tables):

```bash
psql -h localhost -U postgres -d prem_stats -f scripts/reset-warehouse-schema.sql
./mvnw spring-boot:run
```

That drops warehouse tables + `flyway_schema_history`, then V1–V11 run again with the new seed.

---

## Adding a new change later (correct habit)

Do **not** edit old V files in shared/prod DBs. Add a new file:

```text
V7__add_something.sql
```

Example:

```sql
ALTER TABLE warehouse_items ADD COLUMN sku VARCHAR(50);
```

---

## Seed contents (generated for study)

- **2** warehouses (Central, Northern)
- **5** categories
- **4** suppliers
- **12** items (one intentionally `INACTIVE`)
- **17** inventory rows (two intentionally **low stock** for the low-stock API)

All IDs are stable so docs and tests can hard-code them.

---

## Config reminder

See `application.properties`:

- `spring.flyway.enabled=true`
- `spring.jpa.hibernate.ddl-auto=validate` → schema from Flyway, not Hibernate auto-create
- `baseline-on-migrate=true` → allows Flyway when DB also has other tables (`player_*`)
