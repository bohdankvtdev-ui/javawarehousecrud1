import { apiFetch } from "./client";
import type { Item, ItemRequest, ItemSearchParams, PageResponse } from "../types/item";

const BASE = "/api/v1/items";

export function searchItems(
  params: ItemSearchParams = {}
): Promise<PageResponse<Item>> {
  const query = new URLSearchParams();
  if (params.q) query.set("q", params.q);
  if (params.status) query.set("status", params.status);
  if (params.categoryId) query.set("categoryId", params.categoryId);
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 10));
  query.set("sort", params.sort ?? "itemName,asc");

  return apiFetch<PageResponse<Item>>(`${BASE}?${query.toString()}`);
}

export function getItem(id: string): Promise<Item> {
  return apiFetch<Item>(`${BASE}/${id}`);
}

export function createItem(body: ItemRequest): Promise<Item> {
  return apiFetch<Item>(BASE, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function updateItem(id: string, body: ItemRequest): Promise<Item> {
  return apiFetch<Item>(`${BASE}/${id}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export function deleteItem(id: string): Promise<void> {
  return apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" });
}

/** CRM: change only lifecycle status */
export function updateItemStatus(
  id: string,
  status: "ACTIVE" | "INACTIVE" | "DISCONTINUED"
): Promise<Item> {
  return apiFetch<Item>(`${BASE}/${id}/status?status=${status}`, {
    method: "PATCH",
  });
}
