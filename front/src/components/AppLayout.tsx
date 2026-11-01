import { NavLink, Outlet } from "react-router-dom";
import { useEffect, useState } from "react";
import {
  Boxes,
  Building2,
  ChevronLeft,
  ChevronRight,
  ClipboardList,
  LayoutDashboard,
  Lock,
  Package,
  PanelLeft,
  Tags,
  Truck,
  Warehouse,
} from "lucide-react";
import { getDashboardSummary, type DashboardSummary } from "../api/dashboardApi";
import LoginBar from "./LoginBar";

/**
 * STUDY: Asana-style left rail — domains map to backend packages.
 * Compact + keep-alive pages = fast tab switching for learning the API surface.
 */
const NAV = [
  {
    label: "Home",
    items: [{ to: "/", end: true, icon: LayoutDashboard, text: "Overview", hint: "KPIs + API map" }],
  },
  {
    label: "Catalog",
    items: [
      { to: "/items", icon: Package, text: "Items", hint: "WarehouseItem SKU" },
      { to: "/categories", icon: Tags, text: "Categories", hint: "Lookup" },
      { to: "/suppliers", icon: Truck, text: "Suppliers", hint: "Vendors" },
    ],
  },
  {
    label: "Stock",
    items: [
      { to: "/warehouses", icon: Building2, text: "Warehouses", hint: "Places" },
      { to: "/inventory", icon: Boxes, text: "Inventory", hint: "On-hand qty" },
    ],
  },
  {
    label: "CRM",
    items: [
      { to: "/purchase-orders", icon: ClipboardList, text: "Purchase orders", hint: "Inbound" },
      { to: "/reservations", icon: Lock, text: "Reservations", hint: "Soft hold" },
    ],
  },
] as const;

const COLLAPSE_KEY = "wh-sidebar-collapsed";

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return sessionStorage.getItem(COLLAPSE_KEY) === "1";
    } catch {
      return false;
    }
  });
  const [summary, setSummary] = useState<DashboardSummary | null>(null);

  useEffect(() => {
    void getDashboardSummary()
      .then(setSummary)
      .catch(() => undefined);
  }, []);

  function toggleCollapsed() {
    setCollapsed((c) => {
      const next = !c;
      try {
        sessionStorage.setItem(COLLAPSE_KEY, next ? "1" : "0");
      } catch {
        /* ignore */
      }
      return next;
    });
  }

  return (
    <div className={`app-frame ${collapsed ? "app-frame--collapsed" : ""}`}>
      <aside className="sidebar" aria-label="Workspace">
        <div className="sidebar-brand">
          <Warehouse size={20} strokeWidth={1.75} aria-hidden />
          {!collapsed && (
            <div className="sidebar-brand-text">
              <span className="sidebar-title">Warehouse</span>
              <span className="sidebar-sub">CRM study</span>
            </div>
          )}
          <button
            type="button"
            className="sidebar-collapse"
            onClick={toggleCollapsed}
            aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
            title={collapsed ? "Expand" : "Collapse"}
          >
            {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
          </button>
        </div>

        {!collapsed && summary && (
          <div className="sidebar-mini">
            <div className="mini-stat" title="Warehouses">
              <span>WH</span>
              <strong>{summary.warehouseCount}</strong>
            </div>
            <div className="mini-stat" title="Active items">
              <span>Items</span>
              <strong>{summary.activeItemCount}</strong>
            </div>
            <div
              className={`mini-stat ${summary.lowStockCount > 0 ? "mini-stat--warn" : ""}`}
              title="Low stock rows"
            >
              <span>Low</span>
              <strong>{summary.lowStockCount}</strong>
            </div>
          </div>
        )}

        <nav className="sidebar-nav">
          {NAV.map((group) => (
            <div key={group.label} className="sidebar-group">
              {!collapsed && <p className="sidebar-group-label">{group.label}</p>}
              {group.items.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={"end" in item ? item.end : false}
                    title={collapsed ? `${item.text} — ${item.hint}` : item.hint}
                    className={({ isActive }) =>
                      `sidebar-link${isActive ? " active" : ""}`
                    }
                  >
                    <Icon size={17} strokeWidth={2} aria-hidden />
                    {!collapsed && <span>{item.text}</span>}
                  </NavLink>
                );
              })}
            </div>
          ))}
        </nav>

        <div className="sidebar-foot">
          {!collapsed && (
            <p className="sidebar-hint">
              <PanelLeft size={12} aria-hidden /> Maps to Java packages
            </p>
          )}
          <LoginBar compact={collapsed} onAuthChange={() => undefined} />
        </div>
      </aside>

      <div className="workspace">
        <Outlet />
      </div>
    </div>
  );
}
