# Mid-level Java / Spring (2026–2027) — what this CRM teaches

Use this when preparing for **Java middle** interviews. Features below go beyond junior CRUD.

---

## Skills map

| Topic | Where in this project | Interview soundbite |
|-------|----------------------|---------------------|
| Modular packages | `item`, `inventory`, `purchaseorder`, `reservation` | “Bounded contexts inside a modular monolith” |
| Aggregate 1:N | `PurchaseOrder` + `PurchaseOrderLine` | “Header/lines with cascade; mutate via service” |
| `@Transactional` multi-write | PO `receive`, inventory `transfer` | “All-or-nothing across stock + audit” |
| Optimistic locking | `Inventory.@Version` → 409 | “Lost update prevention without pessimistic locks” |
| Soft allocation | `reserved_quantity` + reservations | “Available = on hand − reserved” |
| Domain events | `StockChangedEvent` + `@TransactionalEventListener(AFTER_COMMIT)` | “Side effects only after successful commit” |
| EntityGraph / N+1 | `PurchaseOrderRepository.findWithDetailsById` | “Fetch join graph for detail screens” |
| OpenAPI | springdoc `/swagger-ui.html` | “Contract-first collaboration with frontend/QA” |
| JPA + JDBC | Inventory CRUD vs stock report | “ORM for CRUD, SQL for reports” |
| Flyway evolution | V8–V11 | “Never edit applied migrations; add Vn” |
| Security roles | Basic USER/ADMIN | “AuthZ at the filter chain; next step JWT” |

---

## New CRM flows to demo

1. **Purchase Orders** — create DRAFT → Submit → Receive remaining → Inventory qty↑ + movement RECEIVE  
2. **Reservations** — Reserve 5 → Inventory available↓ → Fulfill → on-hand↓ + SHIP movement  
3. **Transfer** — still under Inventory (two warehouses, one transaction)  
4. **Swagger** — `http://localhost:8080/swagger-ui.html` Authorize as `user`/`user`

---

## Database growth (V8–V11)

| Migration | Tables / columns | Why |
|-----------|------------------|-----|
| V8 | `purchase_orders`, `purchase_order_lines` | Inbound supply chain |
| V9 | `inventory.version`, `inventory.reserved_quantity` | Concurrency + availability |
| V10 | `stock_reservations` | Soft holds |
| V11 | Seed POs | Demo data |

---

## What to learn next (not built yet — good roadmap)

| Next step | Why employers ask |
|-----------|-------------------|
| JWT / OAuth2 Resource Server | Real SPA auth |
| `@PreAuthorize` + method security | Fine-grained AuthZ |
| Spring Data auditing (`@CreatedBy`) | Who changed the row |
| Outbox + Kafka / Rabbit | Reliable integration events |
| QueryDSL / jOOQ reports | Complex analytics |
| Testcontainers Postgres | CI close to prod |
| MapStruct | Compile-time DTO mapping |
| Resilience4j | Timeouts / circuit breaker |
| Docker image + GitHub Actions CI | Deployability |

---

## 60-second oral pitch

> “I built a warehouse CRM modular monolith: catalog CRUD, stock adjust/transfer with an audit table, purchase-order receiving, and soft reservations. Inventory uses optimistic locking. Stock changes publish domain events after commit. Schema is Flyway-versioned; API is documented with OpenAPI. Next I’d swap Basic Auth for JWT and push events to a broker.”
