# Warehouse frontend (study)

React + TypeScript UI for **Warehouse Items**.

Full backend learning guide: see root [`../STUDY.md`](../STUDY.md) and [`../PROJECT_MAP.md`](../PROJECT_MAP.md).

## Run

```bash
npm install
npm run dev
```

Open http://localhost:5173 — Vite proxies `/api` to Spring Boot on `:8080`.

## Routes (React Router)

| Path | Backend APIs |
|------|----------------|
| `/` | Overview of all endpoints |
| `/items` | `/api/v1/items` search + CRUD |
| `/categories` | `/api/v1/categories` |
| `/suppliers` | `/api/v1/suppliers` |
| `/warehouses` | `/api/v1/warehouses` |
| `/inventory` | `/api/v1/inventory` (+ JDBC report) |

Login: top bar → `user/user` or `admin/admin`.

See root `docs/CODE_WALKTHROUGH.md` for how frontend ↔ backend wiring works.
