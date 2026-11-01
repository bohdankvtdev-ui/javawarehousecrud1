# Code walkthrough — how this project really works

Read this with the code open. Goal: know **what each part is for**, **which method does what**, and **what alternatives exist**.

---

## 1. One request, end to end (example)

You type `laptop` in the Items page search box.

```text
React ItemManagement
  debounce 300ms
    → searchItems({ q: "laptop", page: 0 })
      → fetch GET /api/v1/items?q=laptop&page=0&size=10
        → SecurityFilterChain (GET is public)
          → ItemController.searchItems(q, status, categoryId, pageable)
            → ItemService.searchItems(...)
              → ItemSpecs.withFilters(...)          // dynamic WHERE
              → ItemRepository.findAll(spec, page)  // JPA → SQL
                → PostgreSQL warehouse_items
              → map each WarehouseItem → ItemResponse
            → PageResponse JSON
        ← browser renders table page
```

**Create item** is the same path, but:

1. Login bar stored Basic Auth  
2. `POST` requires `USER` or `ADMIN`  
3. `@Valid ItemRequest` may return **400**  
4. Missing category UUID → **404**

---

## 2. Backend layers (what is which)

| Layer | Example class | Job | Should contain? |
|-------|---------------|-----|-----------------|
| **Controller** | `ItemController` | HTTP in/out | Paths, status codes, `@Valid` |
| **DTO** | `ItemRequest` / `ItemResponse` | API contract | Validation annotations |
| **Service** | `ItemService` | Business rules | FK checks, defaults, mapping |
| **Repository** | `ItemRepository` | Persistence | Queries / Specifications |
| **Entity** | `WarehouseItem` | Table row in Java | JPA annotations |
| **Config** | `SecurityConfig` | Cross-cutting | Security, CORS |
| **Advice** | `GlobalExceptionHandler` | Errors → JSON | Exception mapping |

### Restaurant analogy

- Controller = waiter  
- DTO = order ticket  
- Service = chef  
- Repository = fridge access  
- Entity = ingredients on the shelf  

---

## 3. Important methods (Items feature)

### `ItemController.searchItems`

- **Used for:** list UI with filters + pagination  
- **Inputs:** query params `q`, `status`, `categoryId`, plus `page/size/sort`  
- **Returns:** `PageResponse<ItemResponse>`  
- **Alternative:** return `List<>` (bad for big data) or GraphQL connection type  

### `ItemController.createItem`

- **Used for:** Add Item form  
- **Annotation:** `@Valid` → Bean Validation before method runs  
- **Status:** `201 Created`  
- **Security:** needs login (`user` or `admin`)  

### `ItemService.toResponse`

- **Used for:** convert JPA entity → JSON-safe DTO  
- **Why private:** only this service should know entity shape  
- **Touches:** `item.getCategory().getName()` (lazy load → needs `@Transactional`)  

### `ItemService.createItem`

Steps inside:

1. `categoryRepository.findById` — fail → `ResourceNotFoundException`  
2. same for supplier  
3. `new WarehouseItem()` + `applyRequest(...)`  
4. `setStatus(ACTIVE)`  
5. `itemRepository.save` → SQL `INSERT`  
6. `toResponse`  

### `ItemSpecs.withFilters`

- **Used for:** optional WHERE clauses without string-concat SQL  
- **Alternative:** one `@Query` with `(:q IS NULL OR ...)`  
- **Alternative:** QueryDSL / jOOQ  

### `ItemRepository`

- Extends `JpaRepository` → free `save/findById/delete`  
- Extends `JpaSpecificationExecutor` → `findAll(Specification, Pageable)`  

---

## 4. Domain: catalog vs stock

```text
WarehouseItem  = “what product exists” (price, name)     → Items page
Inventory      = “how many in which warehouse”           → Inventory page
```

Deleting inventory does **not** delete the catalog item.  
Deleting a category that still has items may fail on FK (database protects you).

---

## 5. Frontend map (now covers whole backend)

| UI route | Backend | Main API module |
|----------|---------|-----------------|
| `/` | overview of all APIs | — |
| `/items` | catalog search CRUD | `api/itemApi.ts` |
| `/categories` | category CRUD | `api/categoryApi.ts` |
| `/suppliers` | supplier CRUD | `api/supplierApi.ts` |
| `/warehouses` | warehouse CRUD | `api/warehouseApi.ts` |
| `/inventory` | stock + low-stock + JDBC report | `api/inventoryApi.ts` |

Shared pieces:

- `api/client.ts` — `fetch` + auth header + error messages  
- `api/auth.ts` — Basic Auth in `sessionStorage`  
- `components/AppLayout.tsx` — nav + login  
- `components/LoginBar.tsx` — `user/user`, `admin/admin`  

### React flow (Items)

1. `useEffect` loads categories/suppliers for the form  
2. Another `useEffect` depends on `debouncedSearch`, filters, `page` → calls `searchItems`  
3. Table shows `pageData.content`  
4. Save → `createItem` / `updateItem` → bump `reloadTick` → refetch  

**Alternative:** TanStack Query (cache, retries) instead of manual `useEffect` loading.

---

## 6. Security methods / beans

### `SecurityConfig.securityFilterChain`

Defines the filter pipeline:

- CSRF off (stateless JSON API)  
- CORS for Vite `:5173`  
- HTTP Basic  
- Rule table: GET public, write USER, delete ADMIN  

### `userDetailsService`

Creates in-memory `user` and `admin`.  
**Production alternative:** JDBC users, LDAP, or OAuth2 IdP (Keycloak).

### `passwordEncoder`

Delegating encoder (supports `{bcrypt}...`).  
Study passwords are encoded at startup via `passwordEncoder.encode("user")`.

---

## 7. Inventory: JPA vs JDBC (same page, two tabs)

| UI mode | Method / endpoint | Technique |
|---------|-------------------|-----------|
| All stock | `InventoryService.getAll` | JPA |
| By warehouse | `getByWarehouse` | JPA derived query |
| Low stock | `getLowStock` | JPQL `@Query` |
| JDBC report | `InventoryReportJdbcRepository.findStockReport` | `JdbcTemplate` + SQL |
| Adjust / Transfer | `adjust` / `transfer` | JPA + `stock_movements` audit |
| Reorder / Movements | CRM GETs | JPA list + PageRequest |

**Interview line:**  
“CRUD uses JPA; the stock report uses JdbcTemplate so I can show both styles; transfer is `@Transactional` so two warehouses and two audit rows never diverge.”

### Why `@Transactional` on `InventoryService.transfer`

```text
1. qty− on warehouse A
2. qty+ on warehouse B
3. insert TRANSFER_OUT
4. insert TRANSFER_IN
        └── all four in ONE transaction → commit or full rollback
```

Reads use `@Transactional(readOnly = true)` so LAZY `warehouse` / `item` can load while building DTOs.  
Deep dive: [BACKEND_STUDY.md](BACKEND_STUDY.md).

---

## 8. Errors

| Situation | Thrown | Handler | HTTP |
|-----------|--------|---------|------|
| Unknown id | `ResourceNotFoundException` | `handleNotFound` | 404 |
| Duplicate stock pair | `ConflictException` | `handleConflict` | 409 |
| Bad delta / not enough stock | `IllegalArgumentException` | handler | 400 |
| Invalid body | `MethodArgumentNotValidException` | `handleValidation` | 400 |
| No login on POST | Spring Security | default | 401 |
| USER deletes | Spring Security | default | 403 |

---

## 9. Try these examples yourself

```bash
# search
curl -s 'http://localhost:8080/api/v1/items?q=desk&page=0&size=5' | jq .

# categories (UI: /categories)
curl -s http://localhost:8080/api/v1/categories | jq .

# warehouses
curl -s http://localhost:8080/api/v1/warehouses | jq .

# low stock (Central seed id)
curl -s http://localhost:8080/api/v1/inventory/low-stock/a0000000-0000-4000-8000-000000000001 | jq .

# JDBC report
curl -s http://localhost:8080/api/v1/inventory/reports/stock | jq '.[0:3]'

# create as user
curl -s -u user:user -X POST http://localhost:8080/api/v1/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Cat","description":"from curl"}'
```

---

## 10. Suggested reading order in the repo

1. This file + [BACKEND_STUDY.md](BACKEND_STUDY.md) (`@Transactional` section)  
2. `category/` package (simplest CRUD) — `package-info` → Controller → Service → Entity  
3. `ItemController` → `ItemService` → `ItemMapper` → `ItemSpecs` → `WarehouseItem`  
4. `V4` + `V5` + `V7` SQL vs entities  
5. `SecurityConfig` + `GlobalExceptionHandler`  
6. `InventoryService.transfer` + `StockMovement` (CRM transaction example)  
7. `DashboardService` + Overview UI  
8. `front/src/App.tsx` routes → open each page  
9. `docs/JOB_READY.md` for interview phrasing  
