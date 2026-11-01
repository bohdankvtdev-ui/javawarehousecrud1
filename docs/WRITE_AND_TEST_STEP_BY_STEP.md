# From zero: how to write and test code step by step

This is the **practical workflow** (not theory).  
Use it when you start a warehouse / Finance / shop app from an empty folder.

**Rule:** one vertical slice end-to-end before the next feature.  
Never build “all entities first, all APIs later”.

Copy this repo’s **Category** feature as your first slice — it is the simplest full pattern.

---

## Big picture (what “done” means for one feature)

```text
1. Think (paper)     → nouns + one use-case
2. SQL migration     → table exists
3. Entity + Repo     → Java can talk to table
4. Service + DTOs    → rules
5. Controller        → HTTP
6. Manual test       → curl / Swagger / UI
7. Automated test    → unit and/or slice test
8. Only then         → next feature
```

Each step has a **gate**: do not continue until the check passes.

---

## Step 0 — Empty project skeleton (once)

### Write

1. Spring Boot app with:
   - `spring-boot-starter-webmvc` (or web)
   - `data-jpa`, `validation`, `flyway`, Postgres driver
   - later: `security`, `actuator`
2. `application.properties`: DB URL, `ddl-auto=validate`, Flyway on  
3. Packages: `common`, `config`, and first feature e.g. `category`

**Look at this repo:** `pom.xml`, `WareHouseApplication.java`, `application.properties`.

### Test

```bash
./mvnw -q -DskipTests compile
./mvnw spring-boot:run
# → app starts (even with no endpoints yet)
curl -s http://localhost:8080/actuator/health   # after you add actuator
```

**Gate:** context starts without errors.

---

## Step 1 — Think 10 minutes (paper)

For **Category** write:

- Noun: Category (id, name, description)  
- Use-cases: list all, create, update, delete  
- Actor: anyone can read; user can write; admin delete (you can add security later)

**Gate:** you can say the sentences out loud.  
More detail: [BUILD_FROM_SCRATCH.md](BUILD_FROM_SCRATCH.md) Phase A.

---

## Step 2 — Database table (Flyway)

### Write

File: `src/main/resources/db/migration/V2__create_categories.sql`  
(In a new app this might be `V1__…`.)

```sql
CREATE TABLE categories (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
);
```

**Look at:** `V2__create_categories.sql` in this repo.

### Test

```bash
./mvnw spring-boot:run
# Watch logs: Flyway reports "successfully applied"
```

In `psql`:

```sql
\dt
SELECT * FROM categories;
```

**Gate:** table exists; restart does **not** re-apply the same version.

---

## Step 3 — Entity + Repository

### Write

1. `Category.java` — `@Entity`, fields matching columns  
2. `CategoryRepository extends JpaRepository<Category, UUID>`  
3. Optional: `boolean existsByNameIgnoreCase(String name);`

**Look at:** `category/Category.java`, `CategoryRepository.java`.

### Test (still no HTTP)

Add a tiny `@DataJpaTest` **or** temporarily use CommandLineRunner — preferred: repository test like this repo:

**Look at:** `src/test/java/.../CategoryRepositoryTest.java`

```bash
./mvnw -Dtest=CategoryRepositoryTest test
```

**Gate:** test green → save/find works against H2 (or Testcontainers later).

---

## Step 4 — DTOs + Service (business rules)

### Write

1. `CategoryRequest` — `@NotBlank` name  
2. `CategoryResponse` — id, name, description  
3. `CategoryService` — create/update/delete/get  
   - uniqueness check  
   - `@Transactional` on writes  
   - `@Transactional(readOnly = true)` on reads  

**Look at:** `category/dto/*`, `CategoryService.java`.

### Test (unit — no DB)

Mock the repository:

**Look at:** `CategoryServiceTest.java`

```bash
./mvnw -Dtest=CategoryServiceTest test
```

Example ideas:

- create with duplicate name → exception  
- get missing id → `ResourceNotFoundException`

**Gate:** service tests green. You proved rules without Postgres.

---

## Step 5 — Controller (HTTP)

### Write

`CategoryController`:

- `GET /api/v1/categories`  
- `POST /api/v1/categories` → `201`  
- `PUT /api/v1/categories/{id}`  
- `DELETE` → `204`  
- `@Valid` on body  

**Look at:** `CategoryController.java`  
Also add later: `GlobalExceptionHandler` so 404/400 return JSON.

**Look at:** `common/exception/GlobalExceptionHandler.java`

### Test A — manual (fastest feedback)

```bash
./mvnw spring-boot:run

# list
curl -s http://localhost:8080/api/v1/categories | jq .

# create (after security: add -u user:user)
curl -s -X POST http://localhost:8080/api/v1/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo","description":"from curl"}' | jq .

# bad body → 400
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/v1/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":""}'
```

**Gate:** create returns JSON with `id`; empty name returns 400.

### Test B — automated controller slice

`@WebMvcTest(CategoryController.class)` + mock service  
(or follow `ItemControllerTest` pattern in this repo)

```bash
./mvnw -Dtest=ItemControllerTest test
```

**Gate:** status codes asserted without starting full DB.

---

## Step 6 — Security (when writes exist)

### Write

`SecurityConfig`: GET public, POST/PUT authenticated, DELETE admin.

**Look at:** `config/SecurityConfig.java`

### Test

```bash
# POST without auth → 401
curl -s -o /dev/null -w "%{http_code}\n" -X POST http://localhost:8080/api/v1/categories \
  -H 'Content-Type: application/json' \
  -d '{"name":"X"}'

# POST with user → 201
curl -s -u user:user -X POST ...
```

**Gate:** 401 then 201 behave as designed.

---

## Step 7 — Frontend for the same slice

### Write

1. `front/src/types/category.ts`  
2. `front/src/api/categoryApi.ts` — `getCategories`, `createCategory`  
3. Page: list + form  

**Look at:** `front/src/pages/CategoryManagement.tsx`, `api/categoryApi.ts`, `api/client.ts`

### Test

1. `npm run dev`  
2. Open Categories  
3. Create a row → appears in table  
4. Refresh → still there (proves DB, not only React state)

**Gate:** UI and curl show the same data.

---

## Step 8 — Repeat the pattern (muscle memory)

| Next slice | Copy from | New difficulty |
|------------|-----------|----------------|
| Supplier | Category | almost identical |
| Warehouse | Category | almost identical |
| Item | Category + FKs | load category/supplier or 404 |
| Inventory | Item | unique (warehouse,item), qty rules |
| PO receive | Inventory adjust | multi-write `@Transactional` |

**Always the same write order:**

```text
Flyway → Entity → Repo → Service+DTO → Controller → curl → test → UI
```

**Always the same test order:**

```text
compile → (repo test) → service unit test → curl → (WebMvcTest) → UI click
```

---

## How seniors test while coding (daily habit)

| When you change… | Test immediately with… |
|------------------|------------------------|
| SQL migration | App start + `\dt` / select |
| Service rule | Unit test with Mockito |
| HTTP mapping / status | curl or `@WebMvcTest` |
| Full wiring | `@SpringBootTest` (slow, rare) |
| UI | Browser + Network tab |

Do **not** wait until Friday to test everything.

---

## Suggested first weekend from zero

### Saturday morning

1. Skeleton + Category migration  
2. Entity + repo + service + controller  
3. curl green + `CategoryServiceTest` green  

### Saturday afternoon

4. Supplier + Warehouse (copy-paste-rename carefully)  
5. Security Basic  

### Sunday

6. Item with FKs + one ItemServiceTest  
7. React: Categories + Items list/create  
8. Explain out loud: layers + why DTO  

Then inventory / CRM when the above feels boring (that means you learned the pattern).

---

## Correlation: Finance from zero (same steps)

| Warehouse step | Finance rename |
|----------------|----------------|
| Category CRUD | AssetClass CRUD |
| Item + FKs | Instrument + FKs |
| Inventory | Position |
| Adjust / movement | Post ledger entry |
| Reservation | Hold on position |

You still write: migration → entity → service test → curl → UI.  
Only names and business rules change. See [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md).

---

## Cheatsheet — commands you’ll use constantly

```bash
# compile
./mvnw -DskipTests compile

# one test class
./mvnw -Dtest=CategoryServiceTest test

# all tests
./mvnw test

# run API
./mvnw spring-boot:run

# UI
cd front && npm run dev
```

---

## Related docs

| Doc | Use when |
|-----|----------|
| [START_HERE.md](START_HERE.md) | Learning **this** repo |
| [BUILD_FROM_SCRATCH.md](BUILD_FROM_SCRATCH.md) | Decision order / thinking |
| **This file** | Hands on write → test gates |
| [BACKEND_STUDY.md](BACKEND_STUDY.md) | Why `@Transactional` |
| [UML.md](UML.md) | Draw what you built |
