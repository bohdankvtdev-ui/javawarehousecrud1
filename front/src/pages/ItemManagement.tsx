import { useDeferredValue, useEffect, useState } from "react";
import { getCategories } from "../api/categoryApi";
import {
  createItem,
  deleteItem,
  searchItems,
  updateItem,
  updateItemStatus,
} from "../api/itemApi";
import { getSuppliers } from "../api/supplierApi";
import ItemForm from "../components/ItemForm";
import ItemTable from "../components/ItemTable";
import LoadingBlock from "../components/LoadingBlock";
import type { Category } from "../types/category";
import type { Item, ItemRequest, ItemStatus, PageResponse } from "../types/item";
import type { Supplier } from "../types/supplier";

/**
 * STUDY: Server-side search + pagination (not client filter).
 * Debounce (300ms) + useDeferredValue keep typing smooth while requests catch up.
 * Login lives in AppLayout; create/edit/delete need Basic Auth.
 */
export default function ItemManagement() {
  const [pageData, setPageData] = useState<PageResponse<Item> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const deferredSearch = useDeferredValue(debouncedSearch);
  const [status, setStatus] = useState<ItemStatus | "">("");
  const [categoryId, setCategoryId] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState<Item | null>(null);
  const [reloadTick, setReloadTick] = useState(0);
  const searchPending = deferredSearch !== debouncedSearch || search !== debouncedSearch;

  // Debounce search box → fewer HTTP calls while typing
  useEffect(() => {
    const handle = window.setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 300);
    return () => window.clearTimeout(handle);
  }, [search]);

  useEffect(() => {
    void getCategories().then(setCategories).catch(() => undefined);
    void getSuppliers().then(setSuppliers).catch(() => undefined);
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        // Only block UI on first load — keep previous rows while refining search
        setLoading((was) => (pageData ? was : true));
        setError("");
        const data = await searchItems({
          q: deferredSearch || undefined,
          status: status || undefined,
          categoryId: categoryId || undefined,
          page,
          size: 10,
        });
        if (!cancelled) setPageData(data);
      } catch {
        if (!cancelled) {
          setError("Could not load items. Is the backend running on :8080?");
          setPageData(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    void load();
    return () => {
      cancelled = true;
    };
  }, [deferredSearch, status, categoryId, page, reloadTick]);

  async function handleDelete(id: string) {
    if (!window.confirm("Delete this warehouse item? (needs admin/admin)")) return;
    try {
      await deleteItem(id);
      setReloadTick((n) => n + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not delete item.");
    }
  }

  async function handleSave(data: ItemRequest) {
    try {
      if (editing) {
        await updateItem(editing.id, data);
      } else {
        await createItem(data);
      }
      setShowForm(false);
      setEditing(null);
      setReloadTick((n) => n + 1);
    } catch (err) {
      throw err;
    }
  }

  async function handleStatusChange(id: string, next: ItemStatus) {
    try {
      setError("");
      await updateItemStatus(id, next);
      setReloadTick((n) => n + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not update status.");
    }
  }

  const items = pageData?.content ?? [];

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Catalog · GET /api/v1/items</p>
          <h1>Warehouse Items</h1>
          <p>
            Backend entity <code>WarehouseItem</code> — SKU catalog, not stock qty. Search is
            server-side (try <code>ssd</code> / <code>cable</code>).
          </p>
        </div>
        <button type="button" onClick={() => { setEditing(null); setShowForm(true); }}>
          + Add Item
        </button>
      </header>

      <div className="item-stats">
        <div className="stat-card">
          <span>Total (filtered)</span>
          <h2>{pageData?.totalElements ?? "—"}</h2>
        </div>
        <div className="stat-card">
          <span>Page</span>
          <h2>
            {pageData ? pageData.page + 1 : "—"} / {pageData?.totalPages || "—"}
          </h2>
        </div>
        <div className="stat-card">
          <span>Page size</span>
          <h2>{pageData?.size ?? 10}</h2>
        </div>
      </div>

      <div className="filters">
        <input
          type="text"
          placeholder="Search name / type / description…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          value={status}
          onChange={(e) => {
            setStatus(e.target.value as ItemStatus | "");
            setPage(0);
          }}
        >
          <option value="">All statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
          <option value="DISCONTINUED">DISCONTINUED</option>
        </select>
        <select
          value={categoryId}
          onChange={(e) => {
            setCategoryId(e.target.value);
            setPage(0);
          }}
        >
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      {error && <p className="error">{error}</p>}

      {loading && !pageData && <LoadingBlock label="Loading items…" />}

      <div className={loading || searchPending ? "table-dim" : undefined}>
        {pageData && (
          <ItemTable
            items={items}
            onEdit={(item) => {
              setEditing(item);
              setShowForm(true);
            }}
            onDelete={handleDelete}
            onStatusChange={handleStatusChange}
          />
        )}
      </div>
      {(loading || searchPending) && pageData && (
        <p className="hint-inline">Updating results…</p>
      )}

      {pageData && pageData.totalPages > 1 && (
        <div className="pagination">
          <button
            type="button"
            className="btn-secondary"
            disabled={pageData.first}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Previous
          </button>
          <span>
            Page {pageData.page + 1} of {pageData.totalPages}
          </span>
          <button
            type="button"
            className="btn-secondary"
            disabled={pageData.last}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </button>
        </div>
      )}

      {showForm && (
        <ItemForm
          categories={categories}
          suppliers={suppliers}
          initial={editing}
          onSubmit={handleSave}
          onCancel={() => {
            setShowForm(false);
            setEditing(null);
          }}
        />
      )}
    </main>
  );
}
