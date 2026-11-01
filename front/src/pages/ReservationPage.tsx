import { useEffect, useState, type FormEvent } from "react";
import {
  createReservation,
  fulfillReservation,
  listReservations,
  releaseReservation,
} from "../api/reservationApi";
import { searchItems } from "../api/itemApi";
import { getWarehouses } from "../api/warehouseApi";
import type { Reservation } from "../types/reservation";
import type { Item } from "../types/item";
import type { Warehouse } from "../types/warehouse";

/**
 * STUDY: Soft allocation — hold available stock, then release or fulfill (ship).
 */
export default function ReservationPage() {
  const [rows, setRows] = useState<Reservation[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [items, setItems] = useState<Item[]>([]);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [form, setForm] = useState({
    warehouseId: "",
    itemId: "",
    quantity: 1,
    reference: "SO-DEMO-1",
    notes: "",
  });

  async function load() {
    setRows(await listReservations());
  }

  useEffect(() => {
    void getWarehouses().then((w) => {
      setWarehouses(w);
      if (w[0]) setForm((f) => ({ ...f, warehouseId: w[0].id }));
    });
    void searchItems({ page: 0, size: 100 }).then((p) => {
      setItems(p.content);
      if (p.content[0]) setForm((f) => ({ ...f, itemId: p.content[0].id }));
    });
    void load().catch((err) =>
      setError(err instanceof Error ? err.message : "Load failed")
    );
  }, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    try {
      setError("");
      await createReservation(form);
      setInfo("Reserved — available qty decreased on inventory");
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Reserve failed");
    }
  }

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <h1>Stock Reservations</h1>
          <p>
            Soft hold: increases <code>reserved_quantity</code>. Fulfill ships; release frees hold.
          </p>
        </div>
      </header>

      {error && <p className="error">{error}</p>}
      {info && <p className="info">{info}</p>}

      <form className="inline-form crm-panel" onSubmit={onCreate}>
        <select
          value={form.warehouseId}
          onChange={(e) => setForm({ ...form, warehouseId: e.target.value })}
        >
          {warehouses.map((w) => (
            <option key={w.id} value={w.id}>
              {w.name}
            </option>
          ))}
        </select>
        <select
          value={form.itemId}
          onChange={(e) => setForm({ ...form, itemId: e.target.value })}
        >
          {items.map((i) => (
            <option key={i.id} value={i.id}>
              {i.itemName}
            </option>
          ))}
        </select>
        <input
          type="number"
          min={1}
          value={form.quantity}
          onChange={(e) => setForm({ ...form, quantity: Number(e.target.value) })}
        />
        <input
          required
          placeholder="Reference"
          value={form.reference}
          onChange={(e) => setForm({ ...form, reference: e.target.value })}
        />
        <button type="submit">Reserve</button>
      </form>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>Ref</th>
              <th>Warehouse</th>
              <th>Item</th>
              <th>Qty</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{r.reference}</td>
                <td>{r.warehouseName}</td>
                <td>{r.itemName}</td>
                <td>{r.quantity}</td>
                <td>{r.status}</td>
                <td className="actions">
                  {r.status === "ACTIVE" && (
                    <>
                      <button
                        type="button"
                        className="btn-secondary"
                        onClick={() =>
                          void releaseReservation(r.id)
                            .then(load)
                            .catch((err) =>
                              setError(err instanceof Error ? err.message : "Release failed")
                            )
                        }
                      >
                        Release
                      </button>
                      <button
                        type="button"
                        onClick={() =>
                          void fulfillReservation(r.id)
                            .then(() => {
                              setInfo("Fulfilled (shipped)");
                              return load();
                            })
                            .catch((err) =>
                              setError(err instanceof Error ? err.message : "Fulfill failed")
                            )
                        }
                      >
                        Fulfill
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </main>
  );
}
