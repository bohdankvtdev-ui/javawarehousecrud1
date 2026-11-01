# UML for this warehouse CRM (study)

## Read me first (simple)

UML is just **pictures of the system** so your brain does not drown in files.

You do **not** draw every Java class. You draw **three** pictures:

| # | Picture | Question it answers | When to use |
|---|---------|---------------------|-------------|
| 1 | Packages / boxes | What are the big parts? | First slide / intro |
| 2 | ER / domain | What things exist + how they link? | Data model talk |
| 3 | Sequence (+ state) | What happens step-by-step for one action? | “Explain transfer/PO receive” |

**How to view Mermaid diagrams below**

1. Open https://mermaid.live  
2. Paste a ```mermaid block  
3. Or use GitHub / IDE Mermaid preview  

**How this maps to Finance** (same shapes, new names): see [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md).

**Learning order**

1. Look at diagram 2 (ER) while clicking Items vs Inventory in the UI  
2. Look at diagram 3 while clicking **Receive** on a Purchase Order  
3. Only then memorize diagram 1 (packages)

---

## 1. Component / package diagram (modular monolith)

One deployable Spring Boot app; packages = future microservice candidates.

```mermaid
flowchart TB
  subgraph UI["React SPA :5173"]
    Pages["Pages + api/* clients"]
  end

  subgraph App["WareHouseApplication :8080"]
    subgraph Cross["Cross-cutting"]
      Sec["config.SecurityConfig"]
      Err["common.exception.*"]
      Ev["common.event.*"]
      OpenAPI["config.OpenApiConfig"]
    end

    subgraph Catalog["Catalog packages"]
      Cat["category"]
      Sup["supplier"]
      Itm["item"]
    end

    subgraph Stock["Stock packages"]
      Wh["warehouse"]
      Inv["inventory"]
      Dash["dashboard"]
    end

    subgraph CRM["CRM packages"]
      PO["purchaseorder"]
      Res["reservation"]
    end
  end

  DB[(PostgreSQL + Flyway)]

  Pages -->|"HTTP /api/v1 + Basic Auth"| App
  Catalog --> DB
  Stock --> DB
  CRM --> DB
  Inv -.->|"StockChangedEvent"| Ev
  PO -->|"receive()"| Inv
  Res -->|"reserve/fulfill"| Inv
```

**Layer pattern inside each feature package (repeat this, don’t draw 50 classes):**

```mermaid
flowchart LR
  C[Controller] --> S[Service @Transactional]
  S --> R[Repository]
  S --> E[Entity]
  C --> DTO[Request/Response DTOs]
  S --> DTO
```

---

## 2. Domain model (ER / class relationships)

This is the diagram interviewers want for “show me the domain”.  
Cardinality matches JPA/`@ManyToOne` / `@OneToMany`.

```mermaid
erDiagram
  CATEGORY ||--o{ WAREHOUSE_ITEM : "groups"
  SUPPLIER ||--o{ WAREHOUSE_ITEM : "supplies"
  SUPPLIER ||--o{ PURCHASE_ORDER : "ordered from"
  WAREHOUSE ||--o{ INVENTORY : "stores"
  WAREHOUSE_ITEM ||--o{ INVENTORY : "stocked as"
  WAREHOUSE ||--o{ STOCK_MOVEMENT : "at"
  WAREHOUSE_ITEM ||--o{ STOCK_MOVEMENT : "for"
  WAREHOUSE ||--o{ STOCK_RESERVATION : "holds in"
  WAREHOUSE_ITEM ||--o{ STOCK_RESERVATION : "holds"
  WAREHOUSE ||--o{ PURCHASE_ORDER : "receive into"
  PURCHASE_ORDER ||--|{ PURCHASE_ORDER_LINE : "contains"
  WAREHOUSE_ITEM ||--o{ PURCHASE_ORDER_LINE : "line item"

  CATEGORY {
    uuid id PK
    string name
  }
  SUPPLIER {
    uuid id PK
    string name
    string email
  }
  WAREHOUSE {
    uuid id PK
    string name
    string city
  }
  WAREHOUSE_ITEM {
    uuid id PK
    string item_name
    string status
    decimal price
    uuid category_id FK
    uuid supplier_id FK
  }
  INVENTORY {
    uuid id PK
    uuid warehouse_id FK
    uuid item_id FK
    int quantity
    int reserved_quantity
    long version
  }
  STOCK_MOVEMENT {
    uuid id PK
    string movement_type
    int quantity_delta
    int quantity_after
  }
  STOCK_RESERVATION {
    uuid id PK
    int quantity
    string status
    string reference
  }
  PURCHASE_ORDER {
    uuid id PK
    string po_number
    string status
  }
  PURCHASE_ORDER_LINE {
    uuid id PK
    int quantity_ordered
    int quantity_received
  }
```

### How to read it in words

- **Category / Supplier / Warehouse** = master / lookup data  
- **WarehouseItem** = catalog SKU (no quantity)  
- **Inventory** = qty of one SKU in one warehouse (`UQ(warehouse, item)`)  
- **StockMovement** = audit of qty changes  
- **PurchaseOrder + Line** = inbound aggregate  
- **StockReservation** = soft hold on available qty  

**Important:** do **not** put every Controller/Service/DTO on this diagram — that is noise. Domain ER + one package layer diagram is enough.

---

## 3. Sequence — receive purchase order (best CRM story)

```mermaid
sequenceDiagram
  actor User
  participant UI as React PO page
  participant POC as PurchaseOrderController
  participant POS as PurchaseOrderService
  participant Inv as InventoryService
  participant DB as PostgreSQL
  participant Bus as ApplicationEventPublisher
  participant Log as StockChangedEventListener

  User->>UI: Receive remaining
  UI->>POC: POST /purchase-orders/{id}/receive
  POC->>POS: receive(id, lines)
  Note over POS: @Transactional begins
  POS->>DB: load PO + lines (EntityGraph)
  loop each receive line
    POS->>DB: line.quantityReceived += qty
    POS->>Inv: receiveIntoWarehouse(...)
    Inv->>DB: upsert inventory qty
    Inv->>DB: insert stock_movements
    Inv->>Bus: publish StockChangedEvent
  end
  POS->>DB: status PARTIAL or RECEIVED
  Note over POS: commit
  Bus->>Log: AFTER_COMMIT log STOCK_CHANGED
  POS-->>UI: PurchaseOrderResponse
```

### Why this sequence matters

- Shows **Controller thin → Service owns rules**  
- Shows **nested `@Transactional` (REQUIRED)** joining PO + inventory  
- Shows **domain event after commit** (not before)

---

## 4. State diagram — purchase order lifecycle

```mermaid
stateDiagram-v2
  [*] --> DRAFT: POST create
  DRAFT --> ORDERED: submit
  DRAFT --> CANCELLED: cancel
  ORDERED --> PARTIAL: receive (not complete)
  ORDERED --> RECEIVED: receive (all lines done)
  PARTIAL --> PARTIAL: receive more
  PARTIAL --> RECEIVED: last remaining qty
  ORDERED --> CANCELLED: cancel
  PARTIAL --> CANCELLED: cancel
  RECEIVED --> [*]
  CANCELLED --> [*]
```

---

## 5. Optional: class sketch for one feature (Items)

Only draw **one** vertical slice in detail when teaching layering:

```mermaid
classDiagram
  class ItemController {
    +searchItems()
    +createItem()
    +updateStatus()
  }
  class ItemService {
    +searchItems()
    +createItem()
    +updateStatus()
  }
  class ItemRepository
  class ItemMapper
  class ItemSpecs
  class WarehouseItem
  class ItemRequest
  class ItemResponse
  class PageResponse

  ItemController --> ItemService
  ItemController ..> ItemRequest
  ItemController ..> ItemResponse
  ItemController ..> PageResponse
  ItemService --> ItemRepository
  ItemService --> ItemMapper
  ItemService --> ItemSpecs
  ItemMapper ..> WarehouseItem
  ItemMapper ..> ItemResponse
  ItemRepository ..> WarehouseItem
```

Repeat the same shape for `category`, `inventory`, etc. — that is the study pattern.

---

## What to draw in a whiteboard interview (2 minutes)

1. Boxes: **React → API → packages → Postgres**  
2. ER core: **Item – Inventory – Warehouse** (+ Category/Supplier)  
3. One sequence: **PO receive** or **transfer**  
4. Say: “Modular monolith; packages can become services later”

---

## File map (open IDE beside the diagram)

| UML box | First open this | Then this |
|---------|-----------------|-----------|
| Catalog item | `item/package-info.java` | `ItemController.java` |
| Category | `category/package-info.java` | `Category.java` + `V2__…sql` |
| Inventory | `inventory/package-info.java` | `InventoryService.java` |
| PO receive sequence | `purchaseorder/PurchaseOrderService.java` | method `receive` |
| Movements | `inventory/StockMovement.java` | `V7__create_stock_movements.sql` |
| Security box | `config/SecurityConfig.java` | — |
| Events box | `common/event/StockChangedEvent.java` | Listener next to it |

| UML box | Packages |
|---------|----------|
| Catalog | `item/`, `category/`, `supplier/` |
| Stock | `warehouse/`, `inventory/`, `dashboard/` |
| CRM | `purchaseorder/`, `reservation/` |
| Cross-cutting | `config/`, `common/` |

Related: [START_HERE.md](START_HERE.md), [PROJECT_MAP.md](../PROJECT_MAP.md), [BACKEND_STUDY.md](BACKEND_STUDY.md), [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md).
