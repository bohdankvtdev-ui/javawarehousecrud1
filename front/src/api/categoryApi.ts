import { apiFetch } from "./client";
import type { Category, CategoryRequest } from "../types/category";

const BASE = "/api/v1/categories";

/** GET list — public */
export function getCategories(): Promise<Category[]> {
  return apiFetch<Category[]>(BASE);
}

/** POST create — needs USER or ADMIN */
export function createCategory(body: CategoryRequest): Promise<Category> {
  return apiFetch<Category>(BASE, { method: "POST", body: JSON.stringify(body) });
}

/** PUT update — needs USER or ADMIN */
export function updateCategory(id: string, body: CategoryRequest): Promise<Category> {
  return apiFetch<Category>(`${BASE}/${id}`, { method: "PUT", body: JSON.stringify(body) });
}

/** DELETE — needs ADMIN */
export function deleteCategory(id: string): Promise<void> {
  return apiFetch<void>(`${BASE}/${id}`, { method: "DELETE" });
}
