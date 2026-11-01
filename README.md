# Warehouse Study Template

An **advanced but friendly** learning project: Java Spring Boot + PostgreSQL + React warehouse CRM.

You learn layering, Flyway, JPA/JDBC, security, CRM flows (PO, reservations, transfers), and how the **same pattern** maps to Finance / shop / booking.

---

## Start here (do not skip)

1. **[docs/START_HERE.md](docs/START_HERE.md)** ← **learn this repo** (Day 0–6)  
2. **[docs/WRITE_AND_TEST_STEP_BY_STEP.md](docs/WRITE_AND_TEST_STEP_BY_STEP.md)** ← **build from zero** (write → test gates)  
3. **[docs/LEARNING_PATH.md](docs/LEARNING_PATH.md)** — curriculum tracks  
4. **[docs/TRANSFER_TO_OTHER_DOMAINS.md](docs/TRANSFER_TO_OTHER_DOMAINS.md)** — Warehouse → Finance / Shop  

Then go deeper:

| Doc | What it teaches |
|-----|-----------------|
| [STUDY.md](STUDY.md) | How the running system works |
| [docs/BACKEND_STUDY.md](docs/BACKEND_STUDY.md) | Packages + **why `@Transactional`** |
| [PROJECT_MAP.md](PROJECT_MAP.md) | Folders & request flow |
| [docs/CODE_WALKTHROUGH.md](docs/CODE_WALKTHROUGH.md) | One request method-by-method |
| [docs/UML.md](docs/UML.md) | Package / ER / sequence diagrams |
| [docs/BUILD_FROM_SCRATCH.md](docs/BUILD_FROM_SCRATCH.md) | Rebuild from your head |
| [docs/PRINCIPLES.md](docs/PRINCIPLES.md) | OOP, SOLID, smells |
| [docs/DATABASE.md](docs/DATABASE.md) | Flyway + reset |
| [docs/QUERY_OPTIMIZATION.md](docs/QUERY_OPTIMIZATION.md) | Indexes / N+1 before-after |
| [docs/MID_LEVEL.md](docs/MID_LEVEL.md) · [docs/JOB_READY.md](docs/JOB_READY.md) | Interview angle |

Every backend class has **STUDY** javadoc; each feature package has `package-info.java`.

---

## Quick run

```bash
./mvnw spring-boot:run
# other terminal
cd front && npm install && npm run dev
```

- UI: http://localhost:5173 (left sidebar)  
- API docs: http://localhost:8080/swagger-ui.html  
- Login: `user` / `user` (write) · `admin` / `admin` (delete)

```text
React sidebar  →  /api/v1/...  →  Controller  →  Service (@Transactional)  →  Repository  →  PostgreSQL
```
