# Learning path — curriculum tracks

**New here?** Do **[START_HERE.md](START_HERE.md)** first (Day 0–6 in simple words).  
This file is the **compressed track list** after you have run the app once.

Each step: **Do** → **Open** → **Check**.

---

## Track 1 — See the system (1 evening)

| # | Do | Open / run | Check |
|---|----|------------|-------|
| 1 | Run API + UI | [START_HERE.md](START_HERE.md) § Day 0 | Health UP + sidebar visible |
| 2 | Map folders | [PROJECT_MAP.md](../PROJECT_MAP.md) | Sketch Controller→Service→Repo |
| 3 | Create as user | UI Items + login `user/user` | Create returns 201 |
| 4 | Delete as admin | login `admin/admin` | `user` gets 403 on delete |
| 5 | JDBC report | Inventory → JDBC tab | Class: `InventoryReportJdbcRepository` |
| 6 | PO receive | Purchase orders → seed PO | Qty↑ + movement row |
| 7 | Swagger | `/swagger-ui.html` | Can call GET items |
| 8 | Search scale | Items `ssd` / `cable` | Pagination, not full dump |
| 9 | Query story | [QUERY_OPTIMIZATION.md](QUERY_OPTIMIZATION.md) | Seq scan vs GIN / N+1 |

---

## Track 2 — Understand how (2–3 evenings)

| # | Do | Open | Check |
|---|----|------|-------|
| 1 | Transactions | [BACKEND_STUDY.md](BACKEND_STUDY.md) §2 | Why readOnly on GET search? |
| 2 | One request | [CODE_WALKTHROUGH.md](CODE_WALKTHROUGH.md) | Narrate GET `?q=laptop` |
| 3 | Smallest CRUD | `category/package-info.java` → Controller → Service → Entity | Redraw 4 layers |
| 4 | Mapping SRP | `ItemService` + `ItemMapper` | Why not map in controller? |
| 5 | Dynamic query | `ItemSpecs.java` | How blank `q` becomes “no filter”? |
| 6 | Atomic CRM | `InventoryService.transfer` | What rolls back mid-transfer? |
| 7 | Schema versions | [DATABASE.md](DATABASE.md) | Why add V14, not edit V4? |
| 8 | JPA vs JDBC | inventory package + `jdbc/` | When prefer raw SQL? |
| 9 | UML | [UML.md](UML.md) | Draw ER Item–Inventory–Warehouse from memory |

---

## Track 3 — Principles + transfer (2 evenings)

| # | Do | Open | Check |
|---|----|------|-------|
| 1 | SOLID in this repo | [PRINCIPLES.md](PRINCIPLES.md) | Point to SRP / DIP examples |
| 2 | Domain rename | [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md) | Warehouse→Finance table aloud |
| 3 | Exercise | Add filter `supplierId` on items | Specs + controller + UI |
| 4 | Smells | PRINCIPLES smell table | 3 smells + fixes on any repo |
| 5 | Pitch | [JOB_READY.md](JOB_READY.md) + [MID_LEVEL.md](MID_LEVEL.md) | 60s talk without notes |

---

## Track 4 — Rebuild from scratch (weekend)

| # | Do | Open | Check |
|---|----|------|-------|
| 1 | Paper thinking | [BUILD_FROM_SCRATCH.md](BUILD_FROM_SCRATCH.md) Phase A | 8 domain sentences |
| 2 | Slice Category only | new branch | GET/POST works |
| 3 | Slice Item + FKs | same doc Phase B | Form selects category/supplier |
| 4 | Oral exam | BUILD_FROM_SCRATCH end | Teach a friend Category flow |

---

## Track 5 — Forever habit

On any new codebase (Finance, shop, …):

1. Controllers = HTTP nouns?  
2. Services = rules + transactions?  
3. Entities = tables?  
4. Migrations = Flyway/Liquibase?  
5. Audit = movements/ledger?  
6. Map to this template via [TRANSFER_TO_OTHER_DOMAINS.md](TRANSFER_TO_OTHER_DOMAINS.md)

That transfer skill is what mid-level jobs pay for.
