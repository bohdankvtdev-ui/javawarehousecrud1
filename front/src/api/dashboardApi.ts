import { apiFetch } from "./client";

export interface DashboardSummary {
  warehouseCount: number;
  categoryCount: number;
  supplierCount: number;
  itemCount: number;
  activeItemCount: number;
  inventoryRowCount: number;
  lowStockCount: number;
  inventoryValue: number;
  movementCount: number;
}

export function getDashboardSummary(): Promise<DashboardSummary> {
  return apiFetch<DashboardSummary>("/api/v1/dashboard/summary");
}
