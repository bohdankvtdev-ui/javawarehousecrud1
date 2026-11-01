# Backend study guide — every package & key Spring ideas

**New to the repo?** Finish [START_HERE.md](START_HERE.md) Day 0–1 first, then come back here.

Use this with the IDE open on `src/main/java/com/pl/warehouse/`.  
Each package has a `package-info.java`. Each class has a **STUDY** javadoc. Read those first; this file ties them together.

**Same ideas in Finance:** catalog vs position, ledger = movements — [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md).

---

## 1. Mental model (CRM CRUD + a bit more)

```text
Lookup data          Catalog              Stock (CRM)
─────────────        ───────────          ────────────────────────
Category             WarehouseItem        Inventory (qty on hand)
Supplier             (name, price,        StockMovement (audit)
Warehouse             status)             Dashboard (KPIs)
```

**CRUD** = Create / Read / Update / Delete on each noun.  
**CRM extras** = adjust qty, transfer between warehouses, reorder list, movement history, dashboard.

Same pattern everywhere:

```text
Controller  →  Service (@Transactional)  →  Repository  →  DB
     ↑              ↓
   DTOs          Entity (never returned as JSON)
```

---

## 2. Why `@Transactional`? (read this carefully)

### What it does

Spring opens a **database transaction** around the method:

1. Begin transaction  
2. Run your Java code (reads/writes via JPA)  
3. **Commit** if no exception → changes become permanent  
4. **Rollback** if a runtime exception escapes → *nothing* from that method is saved  

Without it (or another transaction boundary), each repository call may auto-commit alone — dangerous for multi-step rules.

### Write methods — `@Transactional`

Example: `InventoryService.transfer`

1. Decrease qty in warehouse A  
2. Increase qty in warehouse B (maybe create row)  
3. Insert `TRANSFER_OUT` movement  
4. Insert `TRANSFER_IN` movement  

If step 4 fails and there is **no** transaction, A already lost stock and B may already have gained it.  
With `@Transactional`, all four succeed or all roll back. **That is the main reason.**

Also used for: create item (load FKs + insert), adjust stock (update + movement), delete.

### Read methods — `@Transactional(readOnly = true)`

Two jobs:

| Job | Why it matters here |
|-----|---------------------|
| **Persistence context** stays open for the method | Lazy associations (`item.getCategory().getName()`) can load while mapping to DTOs. Without a session you get `LazyInitializationException`. |
| **Hint to the DB / Hibernate** | “This won’t write” → can skip dirty checks / use read optimizations. |

So even “just a GET” often needs a transaction when you touch **LAZY** `@ManyToOne` fields after `find`.

### Where we put it

Prefer **service methods**, not controllers:

- Controllers should stay HTTP-only  
- One use-case = one transaction boundary  
- Easier to test services without MockMvc  

### Common mistakes (study checklist)

| Mistake | What goes wrong |
|---------|-----------------|
| Map LAZY entity → DTO **after** the service returns | `LazyInitializationException` |
| Multi-write without `@Transactional` | Partial updates on failure |
| Huge `@Transactional` on the controller | Long DB locks; mixes HTTP + business |
| Catch exceptions inside and swallow them | Rollback may **not** happen (transaction still “successful”) |

Open `ItemService` and `InventoryService` — every public method documents read vs write intent.

---

## 3. File map (study order)

### Entry & config

| File | Learn |
|------|--------|
| `WareHouseApplication.java` | `@SpringBootApplication` starts the context |
| `config/SecurityConfig.java` | Filter chain, Basic Auth, roles, CSRF off |
| `config/CorsConfig.java` | Why browser `:5173` needs CORS (also wired in Security) |
| `config/package-info.java` | Package overview |

### Shared API / errors

| File | Learn |
|------|--------|
| `common/api/PageResponse.java` | Why list endpoints should paginate |
| `common/exception/GlobalExceptionHandler.java` | Exceptions → stable JSON + HTTP status |
| `common/exception/ResourceNotFoundException.java` | → 404 |
| `common/exception/ConflictException.java` | → 409 (duplicate stock row, …) |
| `common/exception/ErrorResponse.java` | Error JSON shape for the UI |

### Feature packages (each has Controller / Service / Repository / Entity / dto)

| Package | Start with | Then |
|---------|------------|------|
| `category/` | `CategoryController` | Service → Entity → DTOs (simplest CRUD) |
| `supplier/` | same pattern | same |
| `warehouse/` | same pattern | referenced by Inventory |
| `item/` | `ItemController` → `ItemService` | `ItemSpecs`, `ItemMapper`, `WarehouseItem` |
| `inventory/` | `InventoryController` → `InventoryService` | `StockMovement*`, `jdbc/InventoryReportJdbcRepository` |
| `dashboard/` | `DashboardController` | aggregation without a separate table |

**Suggested first week:** Category CRUD end-to-end, then Item search/pagination, then Inventory transfer + `@Transactional`.

---

## 4. Annotations cheat sheet (this codebase)

| Annotation | Where | Meaning in one line |
|------------|-------|---------------------|
| `@RestController` | Controller | HTTP adapter; return value → JSON |
| `@RequestMapping` / `@GetMapping`… | Controller | URL + HTTP method |
| `@Valid` | Controller param | Bean Validation before method body |
| `@ResponseStatus` | Controller | e.g. 201 Created, 204 No Content |
| `@Service` | Service | Business bean; transaction boundary |
| `@Transactional` | Service method | Begin/commit/rollback DB work |
| `@Transactional(readOnly=true)` | Service read | Session for LAZY + read hint |
| `@Repository` | Spring Data iface | Persistence API (proxy implements it) |
| `@Entity` / `@Table` | Entity | Class ↔ table |
| `@ManyToOne` / `@JoinColumn` | Entity | FK relationship (often LAZY) |
| `@Enumerated(STRING)` | Entity | Store enum name, not ordinal |
| `@PrePersist` | Entity | Set `createdAt` before INSERT |
| `@Configuration` / `@Bean` | Config | Register infrastructure beans |
| `@RestControllerAdvice` | Exception handler | Global exception → response |
| `@NotBlank` / `@NotNull`… | Request DTO | Validation rules |

---

## 5. CRM methods worth tracing in a debugger

1. **Create item** — `ItemService.createItem` (FK checks + default ACTIVE)  
2. **Search items** — `ItemSpecs` + `PageResponse`  
3. **Upsert stock** — `InventoryService.upsert` (unique warehouse+item)  
4. **Adjust** — relative qty + movement type RECEIVE/SHIP/ADJUST  
5. **Transfer** — two inventories + two movements, one transaction  
6. **Dashboard** — counts + `price * quantity` value  

For each: breakpoint in controller → step into service → watch SQL in logs (`spring.jpa.show-sql` / logger).

---

## 6. How this prepares you for jobs

You can say:

> “I structure apps as modular packages with Controller → Service → Repository. Services own `@Transactional` boundaries so multi-step CRM operations like stock transfer are atomic. The API exposes DTOs, not JPA entities. Cross-cutting concerns live in Security + a global exception handler. Schema is versioned with Flyway.”

Then demo Overview KPIs, Inventory transfer, and Movements tab.

Companion docs: [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md), [PRINCIPLES.md](PRINCIPLES.md), [JOB_READY.md](JOB_READY.md), [DATABASE.md](DATABASE.md).
