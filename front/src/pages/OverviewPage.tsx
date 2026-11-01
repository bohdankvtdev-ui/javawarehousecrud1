import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  Boxes,
  Building2,
  ClipboardList,
  Lock,
  Package,
  Tags,
  Truck,
} from "lucide-react";
import { getDashboardSummary, type DashboardSummary } from "../api/dashboardApi";
import LoadingBlock from "../components/LoadingBlock";

/**
 * STUDY: Full API map per domain — open a card, then open the matching Java package.
 */
const SECTIONS = [
  {
    title: "Items",
    path: "/items",
    icon: Package,
    layer: "Catalog",
    packageName: "com.pl.warehouse.item",
    blurb:
      "Catalog SKU = WarehouseItem (name, type, price, weight, status). Quantity is NOT here — that is Inventory.",
    why: "Separates product master data from stock levels (classic ERP split).",
    apis: [
      {
        method: "GET",
        path: "/api/v1/items?q=&status=&categoryId=&page=&size=&sort=",
        note: "Search + filters + pagination (ItemSpecs). Public.",
      },
      {
        method: "GET",
        path: "/api/v1/items/{id}",
        note: "One item by UUID → 404 if missing.",
      },
      {
        method: "POST",
        path: "/api/v1/items",
        note: "Create SKU. Body: ItemRequest. Needs USER. Status defaults ACTIVE.",
      },
      {
        method: "PUT",
        path: "/api/v1/items/{id}",
        note: "Full update of catalog fields. Needs USER.",
      },
      {
        method: "PATCH",
        path: "/api/v1/items/{id}/status?status=ACTIVE|INACTIVE|DISCONTINUED",
        note: "CRM: change lifecycle only (no full PUT). Needs USER.",
      },
      {
        method: "DELETE",
        path: "/api/v1/items/{id}",
        note: "Remove SKU. Needs ADMIN.",
      },
    ],
  },
  {
    title: "Categories",
    path: "/categories",
    icon: Tags,
    layer: "Catalog",
    packageName: "com.pl.warehouse.category",
    blurb: "Lookup groups (Electronics, Tools…). Items reference category_id.",
    why: "Small CRUD package — best place to learn Controller → Service → Repository first.",
    apis: [
      { method: "GET", path: "/api/v1/categories", note: "List all. Public." },
      { method: "GET", path: "/api/v1/categories/{id}", note: "One category." },
      { method: "POST", path: "/api/v1/categories", note: "Create. Unique name. USER." },
      { method: "PUT", path: "/api/v1/categories/{id}", note: "Update. USER." },
      { method: "DELETE", path: "/api/v1/categories/{id}", note: "Delete. ADMIN. May fail if items still use it (FK)." },
    ],
  },
  {
    title: "Suppliers",
    path: "/suppliers",
    icon: Truck,
    layer: "Catalog",
    packageName: "com.pl.warehouse.supplier",
    blurb: "Vendors we buy from. Linked on Items and Purchase Orders.",
    why: "Same CRUD shape as Category — compare packages for patterns.",
    apis: [
      { method: "GET", path: "/api/v1/suppliers", note: "List all. Public." },
      { method: "GET", path: "/api/v1/suppliers/{id}", note: "One supplier." },
      { method: "POST", path: "/api/v1/suppliers", note: "Create. USER." },
      { method: "PUT", path: "/api/v1/suppliers/{id}", note: "Update. USER." },
      { method: "DELETE", path: "/api/v1/suppliers/{id}", note: "Delete. ADMIN." },
    ],
  },
  {
    title: "Warehouses",
    path: "/warehouses",
    icon: Building2,
    layer: "Stock",
    packageName: "com.pl.warehouse.warehouse",
    blurb: "Physical locations (Central, Northern…). Inventory rows point at a warehouse.",
    why: "Place master data — stock is always “item X in warehouse Y”.",
    apis: [
      { method: "GET", path: "/api/v1/warehouses", note: "List all. Public." },
      { method: "GET", path: "/api/v1/warehouses/{id}", note: "One warehouse." },
      { method: "POST", path: "/api/v1/warehouses", note: "Create. USER." },
      { method: "PUT", path: "/api/v1/warehouses/{id}", note: "Update. USER." },
      { method: "DELETE", path: "/api/v1/warehouses/{id}", note: "Delete. ADMIN." },
    ],
  },
  {
    title: "Inventory",
    path: "/inventory",
    icon: Boxes,
    layer: "Stock",
    packageName: "com.pl.warehouse.inventory",
    blurb:
      "Stock on hand: quantity, reserved, available, @Version. One row per (warehouse, item).",
    why: "Core CRM — adjust/transfer/movements teach @Transactional and audit logs.",
    apis: [
      { method: "GET", path: "/api/v1/inventory", note: "All stock rows (EntityGraph). Public." },
      { method: "GET", path: "/api/v1/inventory/{id}", note: "One stock row." },
      { method: "GET", path: "/api/v1/inventory/by-warehouse/{warehouseId}", note: "Filter by place." },
      { method: "GET", path: "/api/v1/inventory/low-stock/{warehouseId}", note: "qty < min (indexed)." },
      { method: "GET", path: "/api/v1/inventory/reorder-suggestions", note: "CRM: shortage + supplier name." },
      { method: "GET", path: "/api/v1/inventory/movements?limit=20", note: "Audit trail (stock_movements)." },
      { method: "GET", path: "/api/v1/inventory/reports/stock", note: "JdbcTemplate report (not JPA)." },
      { method: "POST", path: "/api/v1/inventory", note: "Strict create — 409 if pair exists. USER." },
      { method: "PUT", path: "/api/v1/inventory/stock", note: "Upsert create-or-update. USER." },
      { method: "PUT", path: "/api/v1/inventory/{id}", note: "Update row. USER." },
      { method: "POST", path: "/api/v1/inventory/{id}/adjust", note: "Body {delta, reason} receive(+)/ship(-). USER." },
      { method: "POST", path: "/api/v1/inventory/transfer", note: "A→B same item, one transaction. USER." },
      { method: "DELETE", path: "/api/v1/inventory/{id}", note: "Delete row. ADMIN. Blocked if reserved>0." },
      { method: "GET", path: "/api/v1/dashboard/summary", note: "KPIs for Overview (separate package)." },
    ],
  },
  {
    title: "Purchase Orders",
    path: "/purchase-orders",
    icon: ClipboardList,
    layer: "CRM",
    packageName: "com.pl.warehouse.purchaseorder",
    blurb: "Inbound supply: header + lines. Receive goods → Inventory qty↑ + movements.",
    why: "Aggregate 1:N + multi-write @Transactional — mid-level interview favorite.",
    apis: [
      { method: "GET", path: "/api/v1/purchase-orders", note: "List POs (summary). Public." },
      { method: "GET", path: "/api/v1/purchase-orders/{id}", note: "Detail + lines (EntityGraph)." },
      { method: "POST", path: "/api/v1/purchase-orders", note: "Create DRAFT with lines. USER." },
      { method: "POST", path: "/api/v1/purchase-orders/{id}/submit", note: "DRAFT → ORDERED. USER." },
      { method: "POST", path: "/api/v1/purchase-orders/{id}/receive", note: "Body {lines:[{lineId,quantity}]} → stock. USER." },
      { method: "POST", path: "/api/v1/purchase-orders/{id}/cancel", note: "Cancel if not fully received. USER." },
    ],
  },
  {
    title: "Reservations",
    path: "/reservations",
    icon: Lock,
    layer: "CRM",
    packageName: "com.pl.warehouse.reservation",
    blurb: "Soft hold: reserved_quantity↑, available↓. Fulfill ships; release frees hold.",
    why: "Teaches available = on hand − reserved without inventing fake stock.",
    apis: [
      { method: "GET", path: "/api/v1/reservations?status=ACTIVE", note: "List (optional status filter)." },
      { method: "POST", path: "/api/v1/reservations", note: "Create hold. USER." },
      { method: "POST", path: "/api/v1/reservations/{id}/release", note: "Undo hold. USER." },
      { method: "POST", path: "/api/v1/reservations/{id}/fulfill", note: "Ship reserved qty. USER." },
    ],
  },
];

function formatMoney(value: number): string {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "EUR",
    maximumFractionDigits: 0,
  }).format(value);
}

export default function OverviewPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void getDashboardSummary()
      .then(setSummary)
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Could not load dashboard")
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Dashboard · GET /api/v1/dashboard/summary</p>
          <h1>How this warehouse fits together</h1>
          <p>
            Each card is a backend package + its HTTP API. Login <code>user/user</code> to write;{" "}
            <code>admin/admin</code> to delete. OpenAPI:{" "}
            <code>http://localhost:8080/swagger-ui.html</code>
          </p>
        </div>
      </header>

      {error && <p className="error">{error}</p>}
      {loading && !summary && <LoadingBlock label="Loading dashboard…" />}

      <section className="flow-board" aria-label="Domain flow">
        <h2 className="section-title">Stock flow</h2>
        <div className="flow-row">
          <Link to="/suppliers" className="flow-node">
            <Truck size={18} /> Suppliers
          </Link>
          <ArrowRight className="flow-arrow" size={18} />
          <Link to="/purchase-orders" className="flow-node flow-node--accent">
            <ClipboardList size={18} /> Purchase Orders
          </Link>
          <ArrowRight className="flow-arrow" size={18} />
          <Link to="/inventory" className="flow-node flow-node--stock">
            <Boxes size={18} /> Inventory
          </Link>
          <ArrowRight className="flow-arrow" size={18} />
          <Link to="/reservations" className="flow-node">
            <Lock size={18} /> Reservations
          </Link>
        </div>
        <div className="flow-row flow-row--secondary">
          <Link to="/categories" className="flow-node flow-node--muted">
            <Tags size={18} /> Categories
          </Link>
          <ArrowRight className="flow-arrow" size={18} />
          <Link to="/items" className="flow-node flow-node--muted">
            <Package size={18} /> Items (catalog)
          </Link>
          <ArrowRight className="flow-arrow" size={18} />
          <Link to="/warehouses" className="flow-node flow-node--muted">
            <Building2 size={18} /> Warehouses
          </Link>
          <span className="flow-note">Item + Warehouse → Inventory row</span>
        </div>
      </section>

      {summary && (
        <div className="kpi-grid">
          <div className="kpi-card">
            <span>Warehouses</span>
            <strong>{summary.warehouseCount}</strong>
          </div>
          <div className="kpi-card">
            <span>Active items</span>
            <strong>
              {summary.activeItemCount}
              <small> / {summary.itemCount}</small>
            </strong>
          </div>
          <div className="kpi-card">
            <span>Stock rows</span>
            <strong>{summary.inventoryRowCount}</strong>
          </div>
          <div className={`kpi-card ${summary.lowStockCount > 0 ? "kpi-warn" : ""}`}>
            <span>Low stock</span>
            <strong>{summary.lowStockCount}</strong>
          </div>
          <div className="kpi-card">
            <span>Inventory value</span>
            <strong>{formatMoney(Number(summary.inventoryValue))}</strong>
          </div>
          <div className="kpi-card">
            <span>Movements</span>
            <strong>{summary.movementCount}</strong>
          </div>
        </div>
      )}

      <h2 className="section-title">Open a domain (full API)</h2>
      <div className="overview-grid overview-grid--wide">
        {SECTIONS.map((section) => {
          const Icon = section.icon;
          return (
            <article key={section.path} className="overview-card overview-card--api">
              <div className="overview-card-top">
                <span className={`layer-chip layer-chip--${section.layer.toLowerCase()}`}>
                  {section.layer}
                </span>
                <Icon size={20} strokeWidth={1.75} aria-hidden />
              </div>
              <h2>
                <Link to={section.path}>{section.title}</Link>
              </h2>
              <p className="overview-blurb">{section.blurb}</p>
              <p className="overview-why">
                <strong>Why study:</strong> {section.why}
              </p>
              <p className="overview-pkg">
                Java: <code>{section.packageName}</code>
              </p>
              <ul className="api-list">
                {section.apis.map((api) => (
                  <li key={api.method + api.path}>
                    <div className="api-line">
                      <span className={`http-method http-method--${api.method.toLowerCase()}`}>
                        {api.method}
                      </span>
                      <code>{api.path}</code>
                    </div>
                    <p className="api-note">{api.note}</p>
                  </li>
                ))}
              </ul>
            </article>
          );
        })}
      </div>

      <p className="hint-block">
        Auth reminder: GET is public; POST/PUT/PATCH need <code>user</code>; DELETE needs{" "}
        <code>admin</code>. Optimistic lock conflicts on inventory → HTTP 409.
      </p>
    </main>
  );
}
