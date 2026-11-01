import { BrowserRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import AppLayout from "./components/AppLayout";
import OverviewPage from "./pages/OverviewPage";
import ItemManagement from "./pages/ItemManagement";
import CategoryManagement from "./pages/CategoryManagement";
import SupplierManagement from "./pages/SupplierManagement";
import WarehouseManagement from "./pages/WarehouseManagement";
import InventoryManagement from "./pages/InventoryManagement";
import PurchaseOrderPage from "./pages/PurchaseOrderPage";
import ReservationPage from "./pages/ReservationPage";

const PAGE_PATHS = [
  "/",
  "/items",
  "/categories",
  "/suppliers",
  "/warehouses",
  "/inventory",
  "/purchase-orders",
  "/reservations",
] as const;

type PagePath = (typeof PAGE_PATHS)[number];

function isPagePath(path: string): path is PagePath {
  return (PAGE_PATHS as readonly string[]).includes(path);
}

/**
 * STUDY: Keep-alive pages — mount once on first visit, then hide/show.
 *
 * Why: remounting Items/Categories on every NavLink click re-ran useEffect fetches
 * and flashed loaders → felt like “micro lag”. Hidden mounted pages keep DOM + state,
 * so switching tabs is instant (like browser tabs).
 */
function PersistentPages() {
  const { pathname } = useLocation();
  const [mounted, setMounted] = useState<Partial<Record<PagePath, boolean>>>({
    "/": true,
  });

  useEffect(() => {
    if (isPagePath(pathname)) {
      setMounted((prev) => (prev[pathname] ? prev : { ...prev, [pathname]: true }));
    }
  }, [pathname]);

  if (!isPagePath(pathname)) {
    return <Navigate to="/" replace />;
  }

  return (
    <>
      {mounted["/"] && (
        <div className={pathname === "/" ? "page-panel" : "page-panel page-panel--hidden"}>
          <OverviewPage />
        </div>
      )}
      {mounted["/items"] && (
        <div className={pathname === "/items" ? "page-panel" : "page-panel page-panel--hidden"}>
          <ItemManagement />
        </div>
      )}
      {mounted["/categories"] && (
        <div
          className={pathname === "/categories" ? "page-panel" : "page-panel page-panel--hidden"}
        >
          <CategoryManagement />
        </div>
      )}
      {mounted["/suppliers"] && (
        <div
          className={pathname === "/suppliers" ? "page-panel" : "page-panel page-panel--hidden"}
        >
          <SupplierManagement />
        </div>
      )}
      {mounted["/warehouses"] && (
        <div
          className={pathname === "/warehouses" ? "page-panel" : "page-panel page-panel--hidden"}
        >
          <WarehouseManagement />
        </div>
      )}
      {mounted["/inventory"] && (
        <div
          className={pathname === "/inventory" ? "page-panel" : "page-panel page-panel--hidden"}
        >
          <InventoryManagement />
        </div>
      )}
      {mounted["/purchase-orders"] && (
        <div
          className={
            pathname === "/purchase-orders" ? "page-panel" : "page-panel page-panel--hidden"
          }
        >
          <PurchaseOrderPage />
        </div>
      )}
      {mounted["/reservations"] && (
        <div
          className={
            pathname === "/reservations" ? "page-panel" : "page-panel page-panel--hidden"
          }
        >
          <ReservationPage />
        </div>
      )}
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="*" element={<PersistentPages />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
