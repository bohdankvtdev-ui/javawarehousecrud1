# Same template → Finance, Shop, Booking (transfer learning)

This warehouse app is a **pattern machine**. If you understand it, you can design other business apps without starting from zero.

---

## 1. Noun translation table

| Warehouse CRM | Finance / Banking | Online shop | Booking / Hotel |
|---------------|-------------------|-------------|-----------------|
| Category | Product type / Asset class | Product category | Room type |
| Supplier | Counterparty / Issuer | Brand / Vendor | Operator |
| WarehouseItem (catalog) | Instrument / Product catalog | Product SKU | Rate plan |
| Warehouse | Book / Legal entity / Portfolio | Warehouse / Store | Property |
| Inventory (qty) | Position / Balance | Stock on hand | Available rooms/slots |
| min_quantity | Limit / threshold | Reorder point | Min allotment |
| StockMovement | Ledger entry / Txn | Stock ledger | Availability change log |
| PurchaseOrder | Trade / Buy order | Purchase order | Allotment contract |
| PO receive | Settlement / Fill | Goods receipt | Confirm allotment |
| Reservation | Hold / Earmark | Cart hold | Booking hold |
| Dashboard KPIs | Exposure / PnL summary | Sales + stock KPIs | Occupancy KPIs |
| Item status ACTIVE | Instrument tradable | Product published | Plan bookable |

**Interview line:**  
> “I used a warehouse domain, but the architecture is domain-agnostic: catalog vs balance, audit movements, inbound orders, soft holds.”

---

## 2. Same packages, new names (example Finance)

```text
com.finance.app
  instrument/     ← was item/
  book/           ← was warehouse/
  position/       ← was inventory/
  ledger/         ← was stock_movements (+ maybe richer)
  order/          ← was purchaseorder/
  hold/           ← was reservation/
  party/          ← was supplier/
  config/ common/ ← same ideas
```

Layering **does not change**:

```text
Controller → Service (@Transactional) → Repository → Entity
     ↑                                        ↓
   DTOs                              Flyway SQL
```

---

## 3. Rules that transfer (copy these habits)

| Habit in this repo | Why it matters in Finance too |
|--------------------|-------------------------------|
| Don’t put balance on the catalog entity | Instrument ≠ position |
| Append-only movement / ledger | Audit & disputes |
| `@Transactional` on multi-leg updates | Transfer / settle must be atomic |
| `@Version` optimistic lock | Two traders updating same position |
| Soft hold (reservation) | Don’t double-spend available balance |
| Flyway versions | Regulated schema change history |
| DTO boundary | Hide internal models from API clients |

---

## 4. Practice exercise (do this on paper)

Pick **Finance**. Rewrite our 8 domain sentences:

1. We have **books** (places money lives).  
2. We have **instruments** (what we can hold).  
3. …  

Then draw:

```text
Party ──< Instrument
              │
              └──< Position >── Book
                     │
                     └── LedgerEntry (audit)
```

Compare to [UML.md](UML.md) ER diagram — same shape.

---

## 5. What would you rename in the UI sidebar?

| Sidebar now | Finance sidebar |
|-------------|-----------------|
| Items | Instruments |
| Warehouses | Books |
| Inventory | Positions |
| Purchase orders | Orders / Trades |
| Reservations | Holds |
| Movements | Ledger |

Same React router pattern; only labels and API paths change.

---

## 6. When you join a real company repo

1. Find the **nouns** (entities / tables)  
2. Find the **audit table** (our `stock_movements`)  
3. Find the **transaction boundaries** (`@Transactional` / Unit of Work)  
4. Find how **schema** is migrated  
5. Map each to this study template — you already know the movie, new cast
