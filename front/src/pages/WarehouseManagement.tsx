import { useEffect, useState, type FormEvent } from "react";
import {
  createWarehouse,
  deleteWarehouse,
  getWarehouses,
  updateWarehouse,
} from "../api/warehouseApi";
import type { Warehouse, WarehouseRequest } from "../types/warehouse";

export default function WarehouseManagement() {
  const [rows, setRows] = useState<Warehouse[]>([]);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState<Warehouse | null>(null);
  const [form, setForm] = useState<WarehouseRequest>({
    name: "",
    address: "",
    city: "",
    capacity: 0,
  });

  async function load() {
    try {
      setError("");
      setRows(await getWarehouses());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Load failed");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  function startCreate() {
    setEditing(null);
    setForm({ name: "", address: "", city: "", capacity: 0 });
  }

  function startEdit(row: Warehouse) {
    setEditing(row);
    setForm({
      name: row.name,
      address: row.address ?? "",
      city: row.city ?? "",
      capacity: row.capacity ?? 0,
    });
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      if (editing) await updateWarehouse(editing.id, form);
      else await createWarehouse(form);
      startCreate();
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  }

  async function onDelete(id: string) {
    if (!window.confirm("Delete warehouse? (admin)")) return;
    try {
      await deleteWarehouse(id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <h1>Warehouses</h1>
          <p>Physical locations. Inventory rows point here.</p>
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
          placeholder="Address"
          value={form.address}
          onChange={(e) => setForm({ ...form, address: e.target.value })}
        />
        <input
          placeholder="City"
          value={form.city}
          onChange={(e) => setForm({ ...form, city: e.target.value })}
        />
        <input
          type="number"
          min={0}
          placeholder="Capacity"
          value={form.capacity ?? 0}
          onChange={(e) => setForm({ ...form, capacity: Number(e.target.value) })}
        />
        <button type="submit">{editing ? "Update" : "Add warehouse"}</button>
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
              <th>City</th>
              <th>Address</th>
              <th>Capacity</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{row.name}</td>
                <td>{row.city}</td>
                <td>{row.address}</td>
                <td>{row.capacity}</td>
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
