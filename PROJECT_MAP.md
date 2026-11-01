# Project Map

Quick visual guide. Details and examples live in [STUDY.md](STUDY.md).

---

## 1. Big picture

```text
┌─────────────────────────────────────────────────────────────────┐
│  Browser (http://localhost:5173)                                │
│  front/  React + TypeScript                                     │
│    pages/ItemManagement  →  api/itemApi  →  fetch(/api/v1/...)  │
└───────────────────────────────┬─────────────────────────────────┘
                                │ Vite proxy /api → :8080
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│  Spring Boot (http://localhost:8080)                            │
│  config/SecurityConfig   (open API for study)                   │
│  common/exception        (404 / 400 JSON errors)                │
│                                                                 │
│  Feature packages (same pattern each):                          │
│    item | category | supplier | warehouse | inventory           │
│                                                                 │
│    Controller  →  Service  →  Repository (JPA)  →  Entity       │
│                      │                                          │
│                      └── inventory also has jdbc/ (JdbcTemplate)│
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│  PostgreSQL                                                     │
│  Flyway: src/main/resources/db/migration/V1…V6                  │
│  Tables: warehouses, categories, suppliers,                     │
│          warehouse_items, inventory                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Folder map (what each place is for)

```text
pl/
├── README.md                 ← entry point
├── STUDY.md                  ← full learning guide
├── PROJECT_MAP.md            ← this file
├── docker-compose.yml        ← optional Postgres
├── pom.xml                   ← Maven dependencies
│
├── src/main/java/com/pl/warehouse/
│   ├── WareHouseApplication.java   ← starts the app
│   ├── config/                     ← Security, CORS
│   ├── common/exception/           ← shared error handling
│   ├── item/                       ← catalog SKUs (WarehouseItem)
│   ├── category/
│   ├── supplier/
│   ├── warehouse/                  ← physical locations
│   └── inventory/                  ← stock quantities
│       └── jdbc/                   ← JdbcTemplate report (study)
│
├── src/main/resources/
│   ├── application.properties      ← DB, JPA, Flyway
│   ├── application-dev.properties  ← optional --spring.profiles.active=dev
│   └── db/migration/               ← SQL schema + seed
│
├── src/test/java/...               ← unit + web + context tests
├── src/test/resources/
│   └── application-test.properties ← H2 in-memory (no Postgres needed)
│
└── front/                          ← React UI for Items
    ├── src/pages/
    ├── src/components/
    ├── src/api/
    └── src/types/
```

---

## 3. One feature = five files (Item example)

```text
item/
  WarehouseItem.java     Entity     = table row in Java
  ItemRepository.java    Repository = talk to DB (Spring Data JPA)
  ItemService.java       Service    = business rules
  ItemController.java    Controller = HTTP endpoints
  dto/
    ItemRequest.java     what client SENDS
    ItemResponse.java    what client GETS
```

Same idea for `category`, `supplier`, `warehouse`, `inventory`.

---

## 4. Domain relationships

```text
  Category ──┐
             ├──► WarehouseItem (catalog: name, price, status…)
  Supplier ──┘           │
                         │ stocked as
                         ▼
                   Inventory ──► Warehouse
                 (quantity, min_quantity, location)
                         │
                         │ every change logs
                         ▼
                   StockMovement (RECEIVE / SHIP / TRANSFER / …)
```

**Example in plain words**

- Item: “Lenovo ThinkPad E14” costs 850 (catalog).
- Warehouse: “Central Warehouse” in Warsaw.
- Inventory: Central Warehouse has **33** ThinkPads (quantity), min 10.
- CRM: adjust +5 → movement RECEIVE; transfer 2 to branch → TRANSFER_OUT + TRANSFER_IN.

---

## 5. HTTP → code path (create item)

```text
POST /api/v1/items
Body: { "itemName": "USB Cable", "categoryId": "...", "supplierId": "...", "price": 5.00, ... }

  1. SecurityFilterChain     allow (permitAll in study mode)
  2. ItemController          @Valid ItemRequest
  3. ItemService.createItem  load Category + Supplier, build WarehouseItem, save
  4. ItemRepository.save     INSERT into warehouse_items
  5. Return ItemResponse     JSON 201 Created
```

If `itemName` is blank → validation → `GlobalExceptionHandler` → **400**.  
If `categoryId` unknown → `ResourceNotFoundException` → **404**.

---

## 6. What technology is used for what

| Piece | Used for |
|-------|----------|
| Spring Web MVC | REST controllers, JSON |
| Spring Data JPA | CRUD repositories + entities |
| JdbcTemplate | Custom SQL stock report |
| Bean Validation | `@NotBlank`, `@NotNull` on DTOs |
| Flyway | Versioned SQL migrations |
| Spring Security | Ready for auth (currently open) |
| Lombok | Generate getters/setters on entities |
| React + TS | Simple Item management UI |
| H2 (tests only) | In-memory DB so `./mvnw test` needs no Postgres |
