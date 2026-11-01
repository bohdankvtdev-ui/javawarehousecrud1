import { apiFetch } from "./client";
import type { Warehouse, WarehouseRequest } from "../types/warehouse";

const BASE = "/api/v1/warehouses";

export function getWarehouses(): Promise<Warehouse[]> {
  return apiFetch<Warehouse[]>(BASE);
}

export function createWarehouse(body: WarehouseRequest): Promise<Warehouse> {
  return apiFetch<Warehouse>(BASE, { method: "POST", body: JSON.stringify(body) });
}

export function updateWarehouse(id: string, body: WarehouseRequest): Promise<Warehouse> {
  return apiFetch<Warehouse>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(body) });
}

export function deleteWarehouse(id: string): Promise<void> {
  return apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" });
}
