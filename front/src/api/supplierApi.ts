import { apiFetch } from "./client";
import type { Supplier, SupplierRequest } from "../types/supplier";

const BASE = "/api/v1/suppliers";

export function getSuppliers(): Promise<Supplier[]> {
  return apiFetch<Supplier[]>(BASE);
}

export function createSupplier(body: SupplierRequest): Promise<Supplier> {
  return apiFetch<Supplier>(BASE, { method: "POST", body: JSON.stringify(body) });
}

export function updateSupplier(id: string, body: SupplierRequest): Promise<Supplier> {
  return apiFetch<Supplier>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(body) });
}

export function deleteSupplier(id: string): Promise<void> {
  return apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" });
}
