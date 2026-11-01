# START HERE — learn this project like a beginner (and keep the mid-level depth)

If docs feel scary: **only this file first**. Do the steps in order.  
Each step says **what to open**, **what to do**, and **how you know it worked**.

You do **not** need to understand everything on day 1. You need a **habit**: look → run → change one tiny thing → explain it in your own words.

---

## What this project is (one paragraph)

A **warehouse CRM study app**:

- **Backend:** Java 21 + Spring Boot (REST API, security, database migrations)  
- **Frontend:** React + TypeScript (calls the API)  
- **Database:** PostgreSQL + Flyway (versioned SQL)

It is a **modular monolith** = one app, many clear packages (`item`, `inventory`, …).  
That same shape works for a **Finance** app, a **shop**, a **booking** system — only the nouns change.

---

## 0. Before anything — install checklist

1. JDK 21+ installed (`java -version`)  
2. Maven wrapper in repo (`./mvnw`)  
3. PostgreSQL running (or Docker: `docker compose up -d`)  
4. Node.js for the UI (`node -v`)

**Config file to peek at:**  
`src/main/resources/application.properties`  
→ DB URL / user / password. Change with env vars if needed (`DB_URL`, `DB_USER`, `DB_PASSWORD`).

---

## Day 0 (30–45 min) — Run it and click around

### Step 0.1 — Start backend

```bash
./mvnw spring-boot:run
```

**Check:** browser or curl → `http://localhost:8080/actuator/health` says UP.  
**If Flyway errors:** read [DATABASE.md](DATABASE.md) or run `scripts/reset-warehouse-schema.sql` then restart.

### Step 0.2 — Start frontend

```bash
cd front
npm install
npm run dev
```

**Check:** open `http://localhost:5173` — left sidebar appears.

### Step 0.3 — Login and poke the CRM

1. Sidebar → login `user` / `user`  
2. **Items** → search `ssd`  
3. **Inventory** → click **Adjust** on one row → Ver should go `0` → `1`  
4. **Purchase orders** → open seed PO → **Receive remaining**  
5. **Overview** → read one API card

**Check aloud:** “Items are the catalog; Inventory is the count; PO receive increases inventory.”

### Step 0.4 — Open Swagger (API playground)

`http://localhost:8080/swagger-ui.html`  
Authorize with Basic `user` / `user`. Try `GET /api/v1/items`.

---

## Day 1 — One request, end to end (the most important mental model)

### Goal

Understand: **UI button → HTTP → Controller → Service → Repository → SQL → JSON back**.

### Step 1.1 — Read this picture

Open [PROJECT_MAP.md](../PROJECT_MAP.md) (5 minutes).

### Step 1.2 — Follow **Category** (smallest feature)

Open these files **in this order** (do not skip):

| # | Open this file | What to notice |
|---|----------------|----------------|
| 1 | `category/package-info.java` | What this package is for |
| 2 | `category/CategoryController.java` | HTTP paths only |
| 3 | `category/CategoryService.java` | Rules + `@Transactional` |
| 4 | `category/Category.java` | Table row in Java (`@Entity`) |
| 5 | `category/CategoryRepository.java` | “Talk to DB” interface |
| 6 | `category/dto/CategoryRequest.java` | What client may send |
| 7 | `category/dto/CategoryResponse.java` | What client receives |
| 8 | `front/src/pages/CategoryManagement.tsx` | UI calling the API |
| 9 | `front/src/api/categoryApi.ts` | `fetch` wrapper |

**SQL for this table:**  
`src/main/resources/db/migration/V2__create_categories.sql`

### Step 1.3 — Say it in simple words

Fill this template (write it down):

> When I click Save on Categories, the browser sends **___** to **___**.  
> Spring runs **___Controller**, which calls **___Service**.  
> Service uses **___Repository** to **INSERT/UPDATE**.  
> Answer comes back as JSON **CategoryResponse**.

### Step 1.4 — Tiny change exercise

In `CategoryService`, temporarily change an error message string, restart, trigger the error.  
**Goal:** prove you know which layer owns messages/rules.

---

## Day 2 — Items = relationships + search (still simple words)

### Why Items are harder than Categories

An Item needs a **Category** and a **Supplier** (foreign keys).  
Quantity is **not** on Item — quantity is on **Inventory**.

### Step 2.1 — File tour

| # | File | Look for |
|---|------|----------|
| 1 | `item/package-info.java` | Catalog vs stock |
| 2 | `item/ItemController.java` | `q`, `status`, `page` |
| 3 | `item/ItemService.java` | Why `@Transactional(readOnly=true)` on search |
| 4 | `item/ItemSpecs.java` | How optional filters become SQL |
| 5 | `item/ItemMapper.java` | Entity → DTO (SRP) |
| 6 | `item/WarehouseItem.java` | `@ManyToOne` category/supplier |
| 7 | `V4__create_warehouse_items.sql` | Table + FKs |

### Step 2.2 — Deep dive docs

Read slowly:

1. [BACKEND_STUDY.md](BACKEND_STUDY.md) section **Why `@Transactional`?**  
2. [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) section on search  

### Step 2.3 — Finance analogy (same pattern!)

| Warehouse | Finance app |
|-----------|-------------|
| Category | Account type / product line |
| Supplier | Counterparty / bank |
| WarehouseItem | Financial product / instrument catalog |
| Warehouse | Legal entity / book |
| Inventory qty | Position / balance |
| StockMovement | Ledger entry / transaction log |
| PurchaseOrder | Trade ticket / order |
| Reservation | Hold / earmark on balance |

**Same code shape:** Controller → Service → Repository → Entity.  
Only names and rules change.

---

## Day 3 — Inventory CRM (where “advanced” starts)

### Step 3.1 — Read with a mission

Open `inventory/InventoryService.java` and find methods:

1. `adjust` — plus/minus qty  
2. `transfer` — warehouse A → B  
3. `receiveIntoWarehouse` — used by Purchase Orders  

For **transfer**, answer:

> If saving the second warehouse fails, what happens to the first?  
> (**Hint:** `@Transactional` → everything rolls back.)

### Step 3.2 — UML for humans

Open [UML.md](UML.md):

1. Look at the **ER diagram** — Item vs Inventory  
2. Look at the **sequence** — PO receive  
3. Look at the **PO state** diagram  

Paste Mermaid into https://mermaid.live if GitHub preview is awkward.

### Step 3.3 — Click + watch logs

With `spring.jpa.show-sql=true`, run a transfer in the UI and watch SQL in the console.  
**Check:** you see updates + inserts for movements.

---

## Day 4 — Database story (Flyway)

### Simple idea

Code does **not** invent tables at runtime in production style.  
**Flyway** runs `V1`, `V2`, … once, in order.

| File | Meaning |
|------|---------|
| `V1`…`V5` | Core tables |
| `V6` | First seed data |
| `V7` | stock_movements |
| `V8`+ | POs, version/reserved, more seed, indexes |

**Rule:** never edit an old `V3` after it ran on a shared DB — add `V14` instead.  
Details: [DATABASE.md](DATABASE.md).

### Optimization (optional same day)

[QUERY_OPTIMIZATION.md](QUERY_OPTIMIZATION.md) — before/after for search and N+1.

---

## Day 5 — Security + job talk

1. Open `config/SecurityConfig.java`  
2. Read [JOB_READY.md](JOB_READY.md)  
3. Practice 60-second pitch from [MID_LEVEL.md](MID_LEVEL.md)

**Simple rules in this app:**

- GET many things → public (easy learning)  
- POST/PUT/PATCH → need `user`  
- DELETE → need `admin`

---

## Day 6+ — Rebuild skill (proves you learned)

**Hands-on write/test loop:** [WRITE_AND_TEST_STEP_BY_STEP.md](WRITE_AND_TEST_STEP_BY_STEP.md)  
(migration → entity → service test → curl → UI — with gates)

Thinking order: [BUILD_FROM_SCRATCH.md](BUILD_FROM_SCRATCH.md)

1. Paper domain sentences  
2. Rebuild **only Category** in a new folder/branch using the write/test doc  
3. Then Supplier  
4. Then Item with FKs  

When stuck: **peek one file**, close it, type from memory.  
That gap = what you still need to study.

---

## How to use the sidebar UI while studying

| Sidebar | Backend package | First file |
|---------|-----------------|------------|
| Overview | `dashboard` + all APIs | Overview page cards |
| Items | `item` | `ItemController.java` |
| Categories | `category` | `CategoryController.java` |
| Suppliers | `supplier` | `SupplierController.java` |
| Warehouses | `warehouse` | `WarehouseController.java` |
| Inventory | `inventory` | `InventoryService.java` |
| Purchase orders | `purchaseorder` | `PurchaseOrderService.java` |
| Reservations | `reservation` | `ReservationService.java` |

Login lives in the sidebar footer (`user/user`, `admin/admin`).

---

## Transfer checklist (open a Finance / Shop codebase)

When you join another project, ask:

1. Where are the **Controllers**? (HTTP)  
2. Where are the **Services**? (rules / `@Transactional`)  
3. Where are the **Entities**? (tables)  
4. Who owns **schema**? (Flyway / Liquibase / Hibernate ddl?)  
5. How is **auth** done? (Basic / JWT / session)  
6. What is the **audit log** equivalent of `StockMovement`?  

Full mapping tables: [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md).

---

## Doc map (after START_HERE)

| If you want… | Open |
|--------------|------|
| Ordered curriculum tracks | [LEARNING_PATH.md](LEARNING_PATH.md) |
| Running system explained | [../STUDY.md](../STUDY.md) |
| `@Transactional` deeply | [BACKEND_STUDY.md](BACKEND_STUDY.md) |
| Diagrams | [UML.md](UML.md) |
| Think before coding | [BUILD_FROM_SCRATCH.md](BUILD_FROM_SCRATCH.md) |
| SOLID / smells | [PRINCIPLES.md](PRINCIPLES.md) |
| Interview features | [JOB_READY.md](JOB_READY.md) · [MID_LEVEL.md](MID_LEVEL.md) |

---

## Golden rules (stick these on a sticky note)

1. **DTOs out, Entities in** — never return JPA entities as JSON in this template.  
2. **Rules in Service** — not in Controller.  
3. **Multi-step writes need `@Transactional`.**  
4. **Catalog ≠ stock** — Item vs Inventory.  
5. **Same pattern everywhere** — Category taught you the pattern; Finance just renames nouns.
