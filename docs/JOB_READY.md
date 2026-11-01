# Job-ready improvements (what / why / interview answers)

This document explains the upgrades added for **Spring Boot + React** interviews and junior/mid jobs.

---

## What we improved in this project

| Area | Before | Now | Why employers care |
|------|--------|-----|--------------------|
| **Search** | Client-side filter on full list | Server `q` + status + category + **page** | Scales; shows you understand query params & DB filtering |
| **Pagination** | Load all rows | `PageResponse` + `page/size/sort` | Standard REST design |
| **Queries** | Simple `findAll` | **JPA Specifications** (`ItemSpecs`) | Dynamic filters without SQL injection / string soup |
| **Security** | `permitAll` | HTTP Basic + **USER / ADMIN** roles | AuthZ basics (who can delete?) |
| **React** | Local filter only | Debounced server search, filters, pagination, login bar | Full-stack thinking |
| **Ops** | Nothing | Actuator `/actuator/health` | “How do you know the app is up?” |
| **CRM stock** | Absolute qty only | Adjust / transfer / movements / reorder / dashboard | Real warehouse workflows + audit trail |
| **Purchase orders** | — | Header/lines + receive → stock | Aggregate + multi-write transaction |
| **Reservations** | — | Soft hold / fulfill | Available qty modeling |
| **Optimistic lock** | — | `Inventory.@Version` → 409 | Concurrency without pessimistic locks |
| **Domain events** | — | `StockChangedEvent` AFTER_COMMIT | Side effects after successful commit |
| **OpenAPI** | — | springdoc Swagger UI | API contract for FE/QA |
| **Docs** | STUDY only | This file + DATABASE.md | Communication skill |

---

## 1. Search & queries (backend)

### Endpoint

```http
GET /api/v1/items?q=laptop&status=ACTIVE&categoryId=...&page=0&size=10&sort=itemName,asc
```

### How it works (simple words)

1. React sends query string  
2. `ItemController` binds `q`, `status`, `categoryId`, and Spring `Pageable`  
3. `ItemService` builds a **Specification** (dynamic WHERE)  
4. DB returns **one page** of rows + total count  
5. Response is `PageResponse` JSON (`content`, `totalElements`, `totalPages`, …)

### Interview soundbite

> “I avoid loading the whole table into the browser. Filters run in the database with JPA Specifications, and the API returns a page object so the UI can paginate.”

### Alternatives you should name

- `@Query` with optional `:param IS NULL OR ...`
- QueryDSL / jOOQ for complex reports  
- Elasticsearch if full-text search becomes huge  

---

## 2. Security (Spring + React)

### What we use: HTTP Basic + roles

| User | Password | Can |
|------|----------|-----|
| `user` | `user` | GET (public anyway), POST, PUT |
| `admin` | `admin` | everything including **DELETE** |

Rules in `SecurityConfig`:

- `GET /api/**` → public (easy browsing/search)
- `POST` / `PUT` → `USER` or `ADMIN`
- `DELETE` → `ADMIN` only
- Stateless sessions + CSRF off (JSON API)

### How React talks to it

Login bar stores `btoa(user:pass)` in `sessionStorage` and `apiFetch` sends:

```http
Authorization: Basic ...
```

### Interview soundbite

> “For the study app I used HTTP Basic and role checks in the filter chain. In production I’d move to JWT or OAuth2 (Keycloak/Auth0), keep tokens out of localStorage if possible, and put auth at the gateway.”

### What to learn next (say this)

1. JWT access + refresh tokens  
2. OAuth2 Resource Server (`spring-boot-starter-oauth2-resource-server`)  
3. `@PreAuthorize` method security  
4. HTTPS everywhere  

---

## 3. React improvements

| Pattern | Why |
|---------|-----|
| Debounced search (300ms) | Don’t spam the API on every keypress |
| Server filters | Same results as Postman; single source of truth |
| Pagination controls | Matches backend `PageResponse` |
| Login bar + 401/403 messages | Shows auth awareness in the UI |
| Typed `PageResponse<T>` | Mirrors backend contract |

### Interview soundbite

> “The UI is a thin client over the API. Search state becomes query params; auth headers are attached centrally in the fetch wrapper.”

### Good next React steps for jobs

- React Router (list / detail / login routes)  
- React Query / TanStack Query (cache, retries)  
- Form library + schema validation (Zod)  
- Shared error boundary / toast notifications  

---

## 4. Actuator (ops)

```http
GET /actuator/health
```

Used by Kubernetes liveness/readiness, load balancers, and “is prod up?” dashboards.

---

## 5. What else hiring managers often ask for

You don’t need all of these tomorrow — know the **names** and the **why**:

| Topic | Why it matters | Status here |
|-------|----------------|-------------|
| OpenAPI / Swagger | Contract for frontend & QA | Documented next step |
| Flyway | Reproducible schema | Done |
| DTO + validation | Safe API boundary | Done |
| Global exception handler | Consistent errors | Done |
| Tests (unit + slice) | Confidence / CI | Done |
| Logging (correlation id) | Debug production | Next |
| Caching (Redis) | Performance | Next |
| Messaging (Kafka) | Async / microservices | Next |
| Docker image of the app | Deployability | Compose has DB only |
| CI (GitHub Actions) | Automated test/build | Next |

---

## 6. Warehouse CRM (advanced stock)

| Feature | Endpoint | Interview angle |
|---------|----------|-----------------|
| Adjust ±qty | `POST .../inventory/{id}/adjust` | Relative update + reject negative stock |
| Transfer | `POST .../inventory/transfer` | Multi-row transaction in one `@Transactional` |
| Movements | `GET .../inventory/movements` | Audit log (why qty changed) |
| Reorder | `GET .../inventory/reorder-suggestions` | Business rule: qty &lt; min |
| Dashboard | `GET .../dashboard/summary` | Aggregation / KPI endpoint |
| Status patch | `PATCH .../items/{id}/status` | Partial update without full PUT |

### Interview soundbite

> “CRUD alone isn’t a warehouse CRM. Adjust and transfer mutate stock with an append-only movement log, reorder suggestions drive purchasing, and the dashboard aggregates value and low-stock counts.”

---

## 7. How to demo in an interview (2 minutes)

1. Open UI → Overview KPIs from `/dashboard/summary`  
2. Search `laptop` → network tab `?q=laptop`  
3. Inventory → Adjust +1 / Transfer between warehouses → open Movements  
4. Try write without login → 401; `admin/admin` for delete  
5. Hit `/actuator/health`  
6. Mention Specifications + `PageResponse` + Flyway + stock_movements  

---

## 8. Quick curl cheatsheet

```bash
# public search
curl -s 'http://localhost:8080/api/v1/items?q=desk&page=0&size=5'

# dashboard KPIs
curl -s 'http://localhost:8080/api/v1/dashboard/summary'

# adjust stock (+5 receive)
curl -s -u user:user -X POST http://localhost:8080/api/v1/inventory/<inventoryId>/adjust \
  -H 'Content-Type: application/json' \
  -d '{"delta":5,"reason":"Receive goods"}'

# transfer between warehouses
curl -s -u user:user -X POST http://localhost:8080/api/v1/inventory/transfer \
  -H 'Content-Type: application/json' \
  -d '{"fromWarehouseId":"...","toWarehouseId":"...","itemId":"...","quantity":2,"reason":"Restock branch"}'

# create as user
curl -s -u user:user -X POST http://localhost:8080/api/v1/items \
  -H 'Content-Type: application/json' \
  -d '{"itemName":"Demo","type":"X","description":"","categoryId":"b0000000-0000-4000-8000-000000000001","supplierId":"c0000000-0000-4000-8000-000000000001","price":1}'

# delete as admin
curl -s -u admin:admin -X DELETE http://localhost:8080/api/v1/items/<id> -i
```
