import { useEffect, useState, type FormEvent } from "react";
import {
  createSupplier,
  deleteSupplier,
  getSuppliers,
  updateSupplier,
} from "../api/supplierApi";
import type { Supplier, SupplierRequest } from "../types/supplier";

export default function SupplierManagement() {
  const [rows, setRows] = useState<Supplier[]>([]);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState<Supplier | null>(null);
  const [form, setForm] = useState<SupplierRequest>({ name: "", email: "", phone: "" });

  async function load() {
    try {
      setError("");
      setRows(await getSuppliers());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Load failed");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function startCreate() {
    setEditing(null);
    setForm({ name: "", email: "", phone: "" });
  }

  function startEdit(row: Supplier) {
    setEditing(row);
    setForm({ name: row.name, email: row.email ?? "", phone: row.phone ?? "" });
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      if (editing) await updateSupplier(editing.id, form);
      else await createSupplier(form);
      startCreate();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  }

  async function onDelete(id: string) {
    if (!window.confirm("Delete supplier? (admin)")) return;
    try {
      await deleteSupplier(id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <h1>Suppliers</h1>
          <p>Vendors linked to warehouse items.</p>
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
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />
        <input
          placeholder="Phone"
          value={form.phone}
          onChange={(e) => setForm({ ...form, phone: e.target.value })}
        />
        <button type="submit">{editing ? "Update" : "Add supplier"}</button>
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
              <th>Email</th>
              <th>Phone</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{row.name}</td>
                <td>{row.email}</td>
                <td>{row.phone}</td>
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
