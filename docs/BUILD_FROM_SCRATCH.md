# Rebuild from scratch — how you should THINK

This is not a copy-paste tutorial. It is the **decision order** professionals use when creating a similar backend + React app from an empty folder.

**First time learning the repo?** Do [START_HERE.md](START_HERE.md) before rebuilding.  
**Want exact write→test commands?** Use [WRITE_AND_TEST_STEP_BY_STEP.md](WRITE_AND_TEST_STEP_BY_STEP.md) beside this file.

Use this doc to recreate *this* warehouse system, **or** rename the nouns and build Finance / shop / booking.

---

## Phase A — Think before typing (30–60 minutes)

### A1. Write the domain in plain sentences

Example for **this** project:

1. We have **warehouses** (places).  
2. We have **items** (catalog products with price).  
3. Items belong to a **category** and a **supplier**.  
4. **Inventory** = how many of an item sit in a warehouse.  
5. Users can **search** items and see **low stock**.  
6. Only admins may **delete**.  
7. We can **transfer** stock between warehouses.  
8. We can **receive** a purchase order into stock.

If you cannot write 5–10 sentences, you are not ready to code.

#### Same exercise for a Finance app (prove transfer learning)

1. We have **books** (portfolios / legal entities).  
2. We have **instruments** (catalog of what we can hold).  
3. Instruments belong to an **asset class** and a **issuer/counterparty**.  
4. **Position** = how much of an instrument we hold in a book.  
5. …  

Full mapping: [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md).

### A2. Draw nouns and relationships

```text
Category ──< Item >── Supplier
              │
              └──< Inventory >── Warehouse
```

Ask: *Is quantity on Item or Inventory?*  
Wrong answer (quantity on Item) breaks multi-warehouse.  
Right answer (Inventory) is why our model works.

### A3. List use-cases (verbs)

| Actor | Use-case |
|-------|----------|
| Anyone | list/search items |
| Anyone | list warehouses, categories… |
| User | create/update item, stock |
| Admin | delete anything |

Each use-case ≈ one service method ≈ one HTTP endpoint (roughly).

### A4. Choose architecture defaults (don’t overthink)

For almost every junior/mid CRUD app:

| Choice | Default | Why |
|--------|---------|-----|
| App shape | Modular monolith | Simple deploy, clear packages |
| API | REST JSON `/api/v1` | Universal |
| DB access | JPA for CRUD | Fast to build |
| Schema | Flyway | Reproducible |
| Security | Roles on write | Realistic |
| UI | SPA calling API | Separates concerns |

Write these choices down. Changing them later is expensive.

---

## Phase B — Vertical slice first (don’t build all tables then all APIs)

**Best learning order:** one thin slice end-to-end, then expand.

### Slice 1 — Category (smallest)

1. SQL table `categories`  
2. Entity `Category`  
3. `CategoryRepository`  
4. `CategoryService` + DTOs  
5. `CategoryController`  
6. React page: list + create  

Prove: `GET/POST /api/v1/categories` works.

**Why start tiny?** You practice the **full pattern** once with little domain noise.

### Slice 2 — Supplier (same pattern)

Copy the pattern, change names. This trains muscle memory.

### Slice 3 — Warehouse

Same pattern again.

### Slice 4 — Item (first real relationships)

Now you need FKs + validation:

1. Table with `category_id`, `supplier_id`  
2. Entity `@ManyToOne`  
3. Service loads category/supplier or 404  
4. Response DTO flattens names  
5. React form with `<select>` of categories/suppliers  

### Slice 5 — Inventory (second relationship type)

Item + Warehouse + quantity. Unique pair constraint.

### Slice 6 — Search / page / security / polish

Only after CRUD works:

- Specifications + `PageResponse`  
- Basic Auth roles  
- Low-stock query  
- One JDBC report for contrast  
- Tests  

**This order matches how seniors grow systems:** working path → more features → cross-cutting concerns.

---

## Phase C — Folder thinking (where does a new file go?)

When you need a new thing, ask:

```text
Is it HTTP?           → controller (+ dto)
Is it a business rule? → service
Is it persistence?    → entity / repository / migration
Is it shared error/security/cors? → common/ or config/
Is it UI?             → front page / api / types
```

Never put SQL in a controller. Never put HTTP status codes in a repository.

---

## Phase D — Implement one endpoint (checklist)

Example: `POST /api/v1/items`

1. **Request DTO** with `@NotBlank` / `@NotNull`  
2. **Service method** `createItem`  
   - load FKs  
   - set defaults (ACTIVE)  
   - save  
   - map to response  
3. **Controller** `@Valid @RequestBody` → `201`  
4. **Security** rule for POST  
5. **Frontend** `createItem()` + form  
6. **Manual test** curl + UI  
7. **Automated test** service unit + controller slice  

If you skip 6 and 7, you don’t know if it works.

---

## Phase E — Database thinking

1. Design tables from relationships (Phase A2).  
2. Write Flyway `V1`, `V2`… in dependency order (parents first).  
3. Align entity field names / nullability with SQL.  
4. Use `ddl-auto=validate` so lies are caught.  
5. Seed with **fixed UUIDs** if you want easy demos.  

**Reset when learning:** drop feature tables + `flyway_schema_history` (see `scripts/reset-warehouse-schema.sql`).

---

## Phase F — Frontend thinking

1. Types that mirror DTOs  
2. `api/*.ts` wrappers around `fetch`  
3. One page per resource (or tabs)  
4. Shared auth header for writes  
5. Server-side search — don’t filter huge lists only in the browser  

Router comes when you have 2+ pages (we use React Router).

---

## Phase G — Recreate THIS project from memory (weekend plan)

### Day 1 — Backend skeleton

- [ ] Empty Spring Boot + Postgres + Flyway  
- [ ] Packages: `category`, `supplier`, `warehouse`, `item`, `inventory`, `common`, `config`  
- [ ] Category + Supplier + Warehouse CRUD  
- [ ] Exception handler + validation  

### Day 2 — Items + inventory

- [ ] Item entity/relations + CRUD  
- [ ] Inventory + unique constraint  
- [ ] Seed migration  
- [ ] Search + pagination + Specs  
- [ ] Security Basic + roles  
- [ ] JDBC report endpoint  

### Day 3 — React + tests

- [ ] Vite React TS  
- [ ] Login + nav + all resource pages  
- [ ] Item search UI  
- [ ] Inventory modes (all / low / JDBC)  
- [ ] Unit + `@WebMvcTest` + `@DataJpaTest`  

### Day 4 — Explain out loud

Without looking, explain:

1. Why Inventory ≠ Item  
2. Why DTO ≠ Entity  
3. Why Specs beat client-side filter  
4. Why USER cannot delete  
5. How you would extract `inventory-service` later  

If you can teach it, you understand it.

---

## Phase H — “From the head” prompts (no IDE)

Answer on paper:

1. Name five packages you’d create for a **library** app (books, members, loans).  
2. Where does “member may borrow max 3 books” live? (service)  
3. What DTO fields for `POST /loans`?  
4. What unique constraint do you need?  
5. What role deletes loans?  

Then compare to how *this* warehouse app placed similar rules.

---

## Common mistakes when rebuilding

| Mistake | Better |
|---------|--------|
| Create all 20 entities first | One vertical slice |
| Return entities from controllers | DTOs |
| Put business rules in React only | Enforce on server |
| `ddl-auto=update` in “real” apps | Flyway + validate |
| Microservices on day 1 | Modular monolith |
| No auth until “later forever” | Protect writes early |
| Huge unstructured seed SQL | Fixed UUID inserts |

---

## Phase I — “I got a Finance ticket Monday”

Use the warehouse code as a cheat sheet:

| Ticket says… | Copy pattern from… |
|--------------|--------------------|
| “Add instrument catalog CRUD” | `category/` or `item/` package |
| “Show position per book” | `inventory/` |
| “Post a ledger movement” | `StockMovement` + `InventoryService.adjust` |
| “Hold balance for pending trade” | `reservation/` |
| “Inbound fill from exchange” | `purchaseorder/` receive |
| “Dashboard exposure” | `dashboard/` |

Open the matching `package-info.java` first, then Controller → Service → Entity.

---

## Link back to principles

While rebuilding, keep [PRINCIPLES.md](PRINCIPLES.md) open.  
After each slice, ask: *Did I respect SRP and DIP?*

Next: [START_HERE.md](START_HERE.md) · [LEARNING_PATH.md](LEARNING_PATH.md).
