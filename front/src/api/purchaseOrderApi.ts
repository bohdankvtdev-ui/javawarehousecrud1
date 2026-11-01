import { apiFetch } from "./client";
import type { PurchaseOrder, PurchaseOrderRequest } from "../types/purchaseOrder";

const BASE = "/api/v1/purchase-orders";

export function listPurchaseOrders(): Promise<PurchaseOrder[]> {
  return apiFetch<PurchaseOrder[]>(BASE);
}

export function getPurchaseOrder(id: string): Promise<PurchaseOrder> {
  return apiFetch<PurchaseOrder>(`${BASE}/${id}`);
}

export function createPurchaseOrder(body: PurchaseOrderRequest): Promise<PurchaseOrder> {
  return apiFetch<PurchaseOrder>(BASE, { method: "POST", body: JSON.stringify(body) });
}

export function submitPurchaseOrder(id: string): Promise<PurchaseOrder> {
  return apiFetch<PurchaseOrder>(`${BASE}/${id}/submit`, { method: "POST" });
}

export function cancelPurchaseOrder(id: string): Promise<PurchaseOrder> {
  return apiFetch<PurchaseOrder>(`${BASE}/${id}/cancel`, { method: "POST" });
}

export function receivePurchaseOrder(
  id: string,
  lines: { lineId: string; quantity: number }[]
): Promise<PurchaseOrder> {
  return apiFetch<PurchaseOrder>(`${BASE}/${id}/receive`, {
    method: "POST",
    body: JSON.stringify({ lines }),
  });
}
