import { useEffect, useState, type FormEvent } from "react";
import {
  createCategory,
  deleteCategory,
  getCategories,
  updateCategory,
} from "../api/categoryApi";
import type { Category, CategoryRequest } from "../types/category";

export default function CategoryManagement() {
  const [rows, setRows] = useState<Category[]>([]);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState<Category | null>(null);
  const [form, setForm] = useState<CategoryRequest>({ name: "", description: "" });

  async function load() {
    try {
      setError("");
      setRows(await getCategories());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Load failed");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function startCreate() {
    setEditing(null);
    setForm({ name: "", description: "" });
  }

  function startEdit(row: Category) {
    setEditing(row);
    setForm({ name: row.name, description: row.description ?? "" });
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      if (editing) await updateCategory(editing.id, form);
      else await createCategory(form);
      startCreate();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  }

  async function onDelete(id: string) {
    if (!window.confirm("Delete category? (admin)")) return;
    try {
      await deleteCategory(id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <h1>Categories</h1>
          <p>Lookup table used when creating warehouse items.</p>
        </div>
      </header>

      {error && <p className="error">{error}</p>}

      <form className="inline-form" onSubmit={onSubmit}>
        <input
          required
          placeholder="Name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <input
          placeholder="Description"
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
        <button type="submit">{editing ? "Update" : "Add category"}</button>
        {editing && (
          <button type="button" className="btn-secondary" onClick={startCreate}>
            Cancel
          </button>
        )}
      </form>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{row.name}</td>
                <td>{row.description}</td>
                <td className="actions">
                  <button type="button" className="btn-secondary" onClick={() => startEdit(row)}>
                    Edit
                  </button>
                  <button type="button" onClick={() => onDelete(row.id)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </main>
  );
}
