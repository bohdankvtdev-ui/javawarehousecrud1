# OOP & design principles — taught by THIS project

Goal: understand **why** the code is shaped this way, so you can reuse the same thinking in **any** Java/Spring (or similar) project.

Concrete classes mentioned below live under `src/main/java/com/pl/warehouse/`.

---

## 0. How to use this document

1. Read one principle.  
2. Open the linked file in the IDE.  
3. Answer out loud: *“If I broke this principle, what would go wrong?”*  
4. Read **Transfer** — how to spot the same problem in other codebases.

---

## 1. OOP building blocks (in our domain)

### Encapsulation

**Idea:** hide internals; expose a clear API.

| Here | How |
|------|-----|
| Entity fields | private + getters/setters (`WarehouseItem`) |
| HTTP contract | DTOs only — clients never see JPA entities |
| Passwords | encoded; not returned in JSON |

**Why good:** you can rename a column or lazy-load strategy without breaking the React app.

**If broken:** frontend depends on Hibernate JSON quirks, circular refs, password leaks.

**Transfer:** In another project, ask: *“What is the public boundary?”* (controller DTO, module API, library interface).

---

### Abstraction

**Idea:** talk in domain language, not SQL/HTTP details.

| Layer | Speaks |
|-------|--------|
| UI | “search items”, “low stock” |
| Service | “create item if category exists” |
| Repository | “find / save” |
| SQL / JDBC | tables and joins |

**Why good:** you can swap JPA ↔ JDBC for one report (`InventoryReportJdbcRepository`) without rewriting the UI.

**Transfer:** Draw three boxes (UI / use-cases / persistence). If a box reaches through another, fix the boundary.

---

### Inheritance vs composition (we mostly compose)

We **compose** services with repositories (constructor injection), we rarely extend classes.

```text
ItemService  HAS-A  ItemRepository
ItemService  HAS-A  CategoryRepository
```

**Why good:** easier to test (mock repos), less fragile than deep class hierarchies.

**When inheritance appears:** Spring Data `extends JpaRepository` (framework inheritance — OK).

**Transfer:** Prefer “has a collaborator” over “extends God class”.

---

### Polymorphism

**Examples here:**

- `Specification<WarehouseItem>` — different filter predicates, same `findAll(spec, page)` API  
- Spring Security `UserDetailsService` — in-memory now; later JDBC/OAuth without rewriting controllers  
- Exception handler methods — different exceptions → different HTTP status, same advice class  

**Transfer:** Same interface, many implementations → swap behavior without rewriting callers.

---

## 2. SOLID (with this repo as the textbook)

### S — Single Responsibility

**One reason to change per unit.**

| Class | One job |
|-------|---------|
| `ItemController` | HTTP for items |
| `ItemService` | item business rules |
| `ItemMapper` | entity ↔ DTO mapping |
| `ItemSpecs` | build search predicates |
| `GlobalExceptionHandler` | turn exceptions into JSON |
| `SecurityConfig` | who may call what |

**Why good:** when search rules change, you edit `ItemSpecs`, not the controller.

**Bad smell in other projects:** a 800-line “Manager” that does HTTP + SQL + email + PDF.

**Fix pattern:** split by *reason to change* (same as our packages: `item/`, `inventory/`, `config/`).

---

### O — Open/Closed

**Open for extension, closed for modification.**

Example: add a new filter `supplierId` → extend `ItemSpecs` + add a request param.  
You rarely rewrite `ItemController`’s overall shape.

Flyway: add `V7__...sql` instead of editing `V4` on a shared DB.

**Transfer:** Prefer adding a new class/migration/strategy over rewriting a working core path.

---

### L — Liskov Substitution

Subtypes must honor the parent contract.

Example: any `UserDetailsService` bean must load users correctly; if your custom impl returns null inconsistently, Security breaks in surprising ways.

Repositories that extend `JpaRepository` must behave like repositories (no hidden “send email” in `save`).

**Transfer:** Don’t make a subclass that throws `UnsupportedOperationException` on half the API.

---

### I — Interface Segregation

**Don’t force clients to depend on methods they don’t use.**

We keep small DTOs (`ItemRequest` ≠ full entity).  
Controllers depend on `ItemService` methods they need, not on a giant “WarehouseGodService”.

**Transfer:** Split fat interfaces (`UserService` with 40 methods) into use-case focused APIs.

---

### D — Dependency Inversion

**High-level policy should not depend on low-level details; both depend on abstractions.**

Here:

- `ItemService` depends on `ItemRepository` **interface** (Spring Data), not on JDBC driver classes.  
- Controllers depend on services, not on `EntityManager`.  
- React depends on HTTP JSON, not on Java classes.

Constructor injection = you pass abstractions in; tests pass mocks.

**Why good:** `ItemServiceTest` runs without Postgres.

**Transfer:** “Can I unit-test this without a database/network?” If no, invert the dependency.

---

## 3. Other principles you should name in interviews

| Principle | Meaning | In this project |
|-----------|---------|-----------------|
| **DRY** | Don’t Repeat Yourself | `applyRequest` / `ItemMapper`; shared `apiFetch` |
| **KISS** | Keep It Simple | Modular monolith before microservices |
| **YAGNI** | You Aren’t Gonna Need It | No Kafka yet — documented as next step |
| **Separation of concerns** | Layers / packages by feature | `item` vs `inventory` vs `config` |
| **Fail fast** | Invalid input → 400 early | `@Valid` on DTOs |
| **Least privilege** | Minimal rights | USER cannot DELETE; ADMIN can |
| **Idempotency awareness** | Careful with POST vs PUT | PUT = update by id; POST = create |

---

## 4. Architectural style: why layered modular monolith?

```text
UI → Controller → Service → Repository → DB
```

**Why this is a strong default for jobs / juniors:**

1. Easy to explain and debug  
2. Matches Spring’s mental model  
3. Each feature package can later become a microservice  
4. Tests map cleanly (web slice / unit / data slice)  

**Not always “best” forever:** huge teams may need modularization (Spring Modulith) or services.  
**Best for learning + most CRUD products:** yes.

**Alternatives:**

| Style | When |
|-------|------|
| Hexagonal / ports & adapters | Complex domain, many external systems |
| CQRS | Very different read vs write models |
| Microservices | Independent deploy / scale / team boundaries |

We prepare for microservices (**package boundaries + DTOs + `/api/v1`**) without paying the cost early.

---

## 5. Problem → principle → solution (cheat sheet for OTHER projects)

| You see this smell | Principle | Do what we did |
|--------------------|-----------|----------------|
| Controller has SQL | Separation / SRP | Move to repository/service |
| Entity returned as JSON | Encapsulation | Introduce Response DTO |
| Copy-paste mapping | DRY / SRP | Mapper class |
| One class does auth + business + mail | SRP | Split packages |
| Can’t test without DB | DIP | Interface + inject mocks |
| UI filters 50k rows in browser | Scalability / SoC | Server search + `PageResponse` |
| Everyone can delete | Least privilege | Roles in SecurityConfig |
| Schema drift on each laptop | Reproducibility | Flyway migrations |
| “God” application.properties secrets | Config hygiene | Env vars |
| Random `RuntimeException` → 500 | Fail clearly | Domain exception + `@RestControllerAdvice` |

Print this table. Use it in code reviews.

---

## 6. Mental model: four questions before writing code

Ask these **every time** (also when rebuilding from scratch):

1. **What is the domain noun?** (Item, Inventory, Order…)  
2. **What is the use-case verb?** (search, create, adjust stock…)  
3. **What is the boundary?** (HTTP DTO? message? UI screen?)  
4. **What must be true?** (validation, auth, invariants like unique warehouse+item)  

Then place code:

```text
verb + noun  → Service method
HTTP         → Controller
persist      → Repository / SQL
shape in/out → DTO / Mapper
rules cross-cutting → Security / Advice / Config
```

---

## 7. Practice exercises (teach yourself)

1. **Add `supplierId` filter** to item search without touching React first — only Specs + controller param. Then wire UI.  
2. **Forbid deleting a category that still has items** — check in `CategoryService` (domain rule), return 400.  
3. **Extract a `LowStockQuery` service** that only reads inventory — SRP for reporting.  
4. **Replace Basic Auth with JWT** in a branch — Dependency Inversion on authentication.  
5. Open a random GitHub Spring project and map their packages to our table in §5.

---

## 8. Related docs

- [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) — method-level “what calls what”  
- [BUILD_FROM_SCRATCH.md](BUILD_FROM_SCRATCH.md) — recreate the app from your head  
- [JOB_READY.md](JOB_READY.md) — interview phrasing  
- [LEARNING_PATH.md](LEARNING_PATH.md) — ordered study plan  
