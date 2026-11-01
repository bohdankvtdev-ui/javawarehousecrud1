import { apiFetch } from "./client";
import type {
  Inventory,
  InventoryRequest,
  InventoryStockReport,
  ReorderSuggestion,
  StockAdjustRequest,
  StockMovement,
  StockTransferRequest,
} from "../types/inventory";

const BASE = "/api/v1/inventory";

export function getInventory(): Promise<Inventory[]> {
  return apiFetch<Inventory[]>(BASE);
}

export function getInventoryByWarehouse(warehouseId: string): Promise<Inventory[]> {
  return apiFetch<Inventory[]>(`${BASE}/by-warehouse/${warehouseId}`);
}

export function getLowStock(warehouseId: string): Promise<Inventory[]> {
  return apiFetch<Inventory[]>(`${BASE}/low-stock/${warehouseId}`);
}

export function getStockReport(): Promise<InventoryStockReport[]> {
  return apiFetch<InventoryStockReport[]>(`${BASE}/reports/stock`);
}

export function getReorderSuggestions(): Promise<ReorderSuggestion[]> {
  return apiFetch<ReorderSuggestion[]>(`${BASE}/reorder-suggestions`);
}

export function getRecentMovements(limit = 20): Promise<StockMovement[]> {
  return apiFetch<StockMovement[]>(`${BASE}/movements?limit=${limit}`);
}

export function createInventory(body: InventoryRequest): Promise<Inventory> {
  return apiFetch<Inventory>(BASE, { method: "POST", body: JSON.stringify(body) });
}

export function upsertInventory(body: InventoryRequest): Promise<Inventory> {
  return apiFetch<Inventory>(`${BASE}/stock`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function adjustInventory(id: string, body: StockAdjustRequest): Promise<Inventory> {
  return apiFetch<Inventory>(`${BASE}/${id}/adjust`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function transferStock(body: StockTransferRequest): Promise<Inventory[]> {
  return apiFetch<Inventory[]>(`${BASE}/transfer`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function updateInventory(id: string, body: InventoryRequest): Promise<Inventory> {
  return apiFetch<Inventory>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(body) });
}

export function deleteInventory(id: string): Promise<void> {
  return apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" });
}
