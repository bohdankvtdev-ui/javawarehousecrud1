# Warehouse Study Guide

This project is a **study template**: a warehouse CRM that teaches Spring Boot layering, databases, React, and patterns you reuse in Finance / shop / booking apps.

## If you feel lost

Open **[docs/START_HERE.md](docs/START_HERE.md)** first. It says exactly which file to open, what to click, and what to say out loud.

## Companion files

- **[docs/START_HERE.md](docs/START_HERE.md)** — beginners: Day 0–6, simple words, file checklists  
- **[docs/WRITE_AND_TEST_STEP_BY_STEP.md](docs/WRITE_AND_TEST_STEP_BY_STEP.md)** — from zero: write code → test each gate  
- **[docs/LEARNING_PATH.md](docs/LEARNING_PATH.md)** — ordered tracks after you ran the app  
- **[docs/TRANSFER_TO_OTHER_DOMAINS.md](docs/TRANSFER_TO_OTHER_DOMAINS.md)** — Warehouse ↔ Finance ↔ Shop  
- **[docs/BACKEND_STUDY.md](docs/BACKEND_STUDY.md)** — every Java package + **why `@Transactional`**  
- **[docs/UML.md](docs/UML.md)** — package / ER / sequence (with “open this file”)  
- **[docs/MID_LEVEL.md](docs/MID_LEVEL.md)** — PO, reservations, `@Version`, events, OpenAPI  
- **[docs/QUERY_OPTIMIZATION.md](docs/QUERY_OPTIMIZATION.md)** — search/N+1/indexes before & after  
- **[PROJECT_MAP.md](PROJECT_MAP.md)** — visual map  
- **[docs/CODE_WALKTHROUGH.md](docs/CODE_WALKTHROUGH.md)** — method-by-method how the code works  
- **[docs/PRINCIPLES.md](docs/PRINCIPLES.md)** — OOP, SOLID, smells  
- **[docs/BUILD_FROM_SCRATCH.md](docs/BUILD_FROM_SCRATCH.md)** — rebuild from zero + Finance rename drill  
- **[docs/JOB_READY.md](docs/JOB_READY.md)** — interview-oriented features  
- **[docs/DATABASE.md](docs/DATABASE.md)** — Flyway, seed IDs, reset

Every backend class under `src/main/java/com/pl/warehouse/` has a **STUDY** javadoc. Packages have `package-info.java`.

---

## Table of contents

1. [What you are building](#1-what-you-are-building)
2. [How to run](#2-how-to-run)
3. [Step-by-step: one request](#3-step-by-step-one-request)
4. [Layers explained (with examples)](#4-layers-explained-with-examples)
5. [Database & Flyway](#5-database--flyway)
6. [JPA vs JDBC (and alternatives)](#6-jpa-vs-jdbc-and-alternatives)
7. [Security (HTTP Basic + roles)](#7-security-http-basic--roles)
8. [Frontend](#8-frontend)
9. [Tests](#9-tests)
10. [Path to microservices](#10-path-to-microservices)
11. [Suggested learning order](#11-suggested-learning-order)

---

## 1. What you are building

A **warehouse catalog + stock** system.

| Word in the app | Meaning | Example |
|-----------------|---------|---------|
| **Warehouse** | Physical place | Central Warehouse, Warsaw |
| **Category** | Group for items | Electronics |
| **Supplier** | Who you buy from | TechSource Europe |
| **WarehouseItem** | Catalog SKU (not stock count) | Lenovo ThinkPad E14, price 850 |
| **Inventory** | How many of an item in a warehouse | 33 ThinkPads in Central, min 10 |
| **StockMovement** | Audit row when qty changes | RECEIVE +10, TRANSFER_OUT −5 |
| **Dashboard** | CRM KPIs in one call | low-stock count, inventory value |

There is **no Product entity**. The UI and API say **Item** / **Warehouse Item**.

```text
Category + Supplier → describe → WarehouseItem
WarehouseItem + Warehouse → stocked in → Inventory (quantity)
Inventory changes → append → StockMovement (audit)
```

### Warehouse CRM operations (beyond CRUD)

| Operation | Endpoint | Why it matters |
|-----------|----------|----------------|
| Upsert stock | `PUT /api/v1/inventory/stock` | One row per warehouse+item |
| Adjust (±) | `POST /api/v1/inventory/{id}/adjust` | Receive / ship without rewriting absolute qty |
| Transfer | `POST /api/v1/inventory/transfer` | Move stock warehouse A → B atomically |
| Reorder list | `GET /api/v1/inventory/reorder-suggestions` | qty &lt; min + supplier to call |
| Movement history | `GET /api/v1/inventory/movements` | Who/what changed stock |
| Dashboard | `GET /api/v1/dashboard/summary` | Counts + inventory value |
| Item status | `PATCH /api/v1/items/{id}/status` | Discontinue without full PUT |

---

## 2. How to run

### Database

This project is configured for local Postgres. Defaults in `application.properties` currently target DB `prem_stats` (your existing DB). Override anytime:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/prem_stats
export DB_USER=postgres
export DB_PASSWORD=yourpassword
```

Optional Docker DB (`warehouse`):

```bash
docker compose up -d
export DB_URL=jdbc:postgresql://localhost:5432/warehouse
export DB_PASSWORD=postgres
```

### Backend

```bash
./mvnw spring-boot:run
```

API base: `http://localhost:8080/api/v1`

### Frontend

```bash
cd front
npm install
npm run dev
```

UI: `http://localhost:5173` (Vite proxies `/api` → backend).

### Tests (no Postgres needed)

```bash
./mvnw test
```

Uses H2 in-memory via `application-test.properties`.

---

## 3. Step-by-step: one request

**Goal:** create a new catalog item from the UI or with curl.

### Example HTTP call

```bash
curl -s -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "itemName": "USB-C Cable 2m",
    "type": "Cable",
    "description": "Study example item",
    "categoryId": "<paste-a-real-category-uuid>",
    "supplierId": "<paste-a-real-supplier-uuid>",
    "price": 7.50,
    "weight": 0.1
  }'
```

First get IDs:

```bash
curl -s http://localhost:8080/api/v1/categories | head
curl -s http://localhost:8080/api/v1/suppliers | head
```

### What happens inside Spring (simple words)

```text
1. Browser/curl sends JSON to /api/v1/items
2. Security allows it (study mode = everyone allowed)
3. ItemController receives body as ItemRequest
4. @Valid checks: name not blank, price not null, …
   - fail → 400 + ErrorResponse (GlobalExceptionHandler)
5. ItemService.createItem:
   - find Category by id (missing → 404)
   - find Supplier by id (missing → 404)
   - new WarehouseItem, copy fields, status = ACTIVE
   - itemRepository.save(...) → SQL INSERT
6. Convert entity → ItemResponse (includes categoryName, supplierName)
7. Controller returns 201 Created + JSON
```

**UI path:** Add Item form → `createItem()` in `front/src/api/itemApi.ts` → same API.

---

## 4. Layers explained (with examples)

Think of a restaurant:

| Layer | Restaurant analogy | In this project |
|-------|--------------------|-----------------|
| **Controller** | Waiter takes the order | `ItemController` |
| **DTO** | Menu ticket (not the kitchen pot) | `ItemRequest` / `ItemResponse` |
| **Service** | Chef cooks using rules | `ItemService` |
| **Repository** | Fridge / pantry access | `ItemRepository` |
| **Entity** | Food itself | `WarehouseItem` |
| **DB table** | Shelf label | `warehouse_items` |

### Why DTOs instead of returning the Entity?

- Hide internal fields and lazy relationships
- Validate input (`@NotBlank`) without polluting the entity
- Stable API if you rename columns later

**Bad (for learning APIs):** `@GetMapping` returns `WarehouseItem`  
**Good (this template):** returns `ItemResponse`

### Why Service between Controller and Repository?

```text
Controller: “HTTP stuff only”
Service:    “business rules” (check category exists, set ACTIVE, …)
Repository: “save / find / delete”
```

If tomorrow Inventory rules get complex (cannot delete item with stock), that logic belongs in a **service**, not in the controller.

### Annotations you will see often

| Annotation | Meaning |
|------------|---------|
| `@RestController` | This class handles HTTP + returns JSON |
| `@RequestMapping("/api/v1/items")` | Base URL path |
| `@Valid` | Run Bean Validation on the body |
| `@Service` | Spring-managed business bean |
| `@Transactional` | One DB transaction for the method (all-or-nothing) |
| `@Transactional(readOnly = true)` | Read path: keep JPA session open for LAZY loads |
| `@Entity` / `@Table` | JPA maps class ↔ table |
| `@Repository` | Spring Data JPA interface |

### Why `@Transactional`? (short version)

- **Writes:** several DB changes in one use-case must commit together (e.g. stock transfer = 2 inventory updates + 2 movement inserts). Fail midway → **rollback everything**.  
- **Reads (`readOnly`):** mapping DTOs often touches LAZY relations (`item.getSupplier().getName()`). The transaction keeps the persistence context open so you do not get `LazyInitializationException`.  

Put it on **service** methods, not controllers. Full explanation: **[docs/BACKEND_STUDY.md](docs/BACKEND_STUDY.md)**.

---

## 5. Database & Flyway

Full guide: **[docs/DATABASE.md](docs/DATABASE.md)** (how to use, reset, seed IDs, curl examples).

### Flyway = versioned SQL scripts

| File | Job |
|------|-----|
| `V1` … `V5` | Create tables (+ indexes, checks) |
| `V6` | **Simple seed** with fixed UUIDs (easy to copy into curl) |
| `V7` | `stock_movements` audit table (CRM history) |

On startup Flyway looks at `flyway_schema_history` and runs only **new** versions.

### `ddl-auto=validate`

Hibernate **checks** entities match the DB. It does **not** create tables.  
Schema source of truth = Flyway.

### Reset after editing migrations (study)

```bash
psql -h localhost -U postgres -d prem_stats -f scripts/reset-warehouse-schema.sql
./mvnw spring-boot:run
```

### Common error

> Found non-empty schema but no schema history table  

→ DB has tables but no Flyway history. This project uses `baseline-on-migrate=true` for that case.

---

## 6. JPA vs JDBC (and alternatives)

### JPA (main path) — example

```java
public interface ItemRepository extends JpaRepository<WarehouseItem, UUID> {
    // Spring writes the SQL from the method name:
    Page<WarehouseItem> findByItemNameContainingIgnoreCase(String itemName, Pageable pageable);
}
```

You work with **objects**; Spring/Hibernate generates SQL for CRUD.

### JDBC (study path) — example

`GET /api/v1/inventory/reports/stock` → `InventoryReportJdbcRepository`:

```java
jdbcTemplate.query(sql, (rs, rowNum) -> new InventoryStockReport(...));
```

You write **SQL yourself**; good for reports and complex joins.

### When to use what

| Technique | Use when |
|-----------|----------|
| **Spring Data JPA** | Normal CRUD, relationships, most services |
| **JdbcTemplate** | Reports, batches, exact SQL control |
| Spring Data JDBC | Want repositories without JPA session/lazy magic |
| MyBatis | Lots of dynamic SQL / XML mappers |
| jOOQ | Type-safe SQL DSL generated from schema |
| Liquibase | Alternative to Flyway for migrations |

---

## 7. Security (HTTP Basic + roles)

See **[docs/JOB_READY.md](docs/JOB_READY.md)** for interview talking points.

| User | Password | Rights |
|------|----------|--------|
| *(none)* | | GET / search only |
| `user` | `user` | POST + PUT |
| `admin` | `admin` | POST + PUT + DELETE |

Next career step: JWT / OAuth2 Resource Server (SPA + microservices).

---

## 8. Frontend (full backend coverage)

| Route | Page | Backend |
|-------|------|---------|
| `/` | Overview | dashboard KPIs + API map |
| `/items` | ItemManagement | search + CRUD + status patch |
| `/categories` | CategoryManagement | CRUD |
| `/suppliers` | SupplierManagement | CRUD |
| `/warehouses` | WarehouseManagement | CRUD |
| `/inventory` | InventoryManagement | upsert, adjust, transfer, reorder, movements, JDBC report |
| `/purchase-orders` | PurchaseOrderPage | DRAFT → submit → receive into stock |
| `/reservations` | ReservationPage | soft hold / release / fulfill |

Login bar (HTTP Basic) is shared in `AppLayout`. Deletes need `admin/admin`.

Details: **[docs/CODE_WALKTHROUGH.md](docs/CODE_WALKTHROUGH.md)**.

---

## 9. Tests

| Test class | Type | What it teaches |
|------------|------|-----------------|
| `ItemServiceTest` | Unit (Mockito) | Business logic without DB |
| `ItemControllerTest` | `@WebMvcTest` | HTTP status codes + JSON |
| `CategoryRepositoryTest` | `@DataJpaTest` | Persistence slice + H2 |
| `WareHouseApplicationTests` | `@SpringBootTest` | Full context starts |

Run:

```bash
./mvnw test
```

---

## 10. Path to microservices

Today this is a **modular monolith** (one app, clear packages).

Later extraction checklist:

1. Keep talking through **services**, not other controllers’ internals  
2. Keep `/api/v1` DTOs as the public contract  
3. Each new service → **own database** + own Flyway  
4. Replace local calls with HTTP/gRPC or events  
5. Auth at API gateway; services validate JWT  

Do not split early — learn boundaries first.

---

## 11. Suggested learning order

1. **[docs/START_HERE.md](docs/START_HERE.md)** — Day 0 run app, Day 1 open Category files in order  
2. **[docs/LEARNING_PATH.md](docs/LEARNING_PATH.md)** — tracks 1–5  
3. **[docs/TRANSFER_TO_OTHER_DOMAINS.md](docs/TRANSFER_TO_OTHER_DOMAINS.md)** — same design for Finance/Shop  
4. Rebuild: **[docs/BUILD_FROM_SCRATCH.md](docs/BUILD_FROM_SCRATCH.md)**  

Short version after START_HERE:

1. `category/` package-info → Controller → Service → Entity  
2. [BACKEND_STUDY.md](docs/BACKEND_STUDY.md) — `@Transactional`  
3. Items + Specs; Inventory transfer; [UML.md](docs/UML.md) PO receive sequence  
4. PRINCIPLES → oral demo (JOB_READY / MID_LEVEL)  

---

## API cheat sheet

| Method | Path |
|--------|------|
| CRUD + status | `/api/v1/items` (+ `PATCH .../status`) |
| CRUD | `/api/v1/categories` |
| CRUD | `/api/v1/suppliers` |
| CRUD | `/api/v1/warehouses` |
| CRUD + CRM | `/api/v1/inventory` (stock, adjust, transfer, reorder, movements) |
| KPIs | `/api/v1/dashboard/summary` |
| Purchase orders | `/api/v1/purchase-orders` (+ submit/receive/cancel) |
| Reservations | `/api/v1/reservations` (+ release/fulfill) |
| OpenAPI UI | `/swagger-ui.html` |
| GET | `/api/v1/inventory/low-stock/{warehouseId}` |
| GET | `/api/v1/inventory/reports/stock` ← JDBC |
