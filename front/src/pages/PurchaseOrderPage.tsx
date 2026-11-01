import { useEffect, useState, type FormEvent } from "react";
import {
  cancelPurchaseOrder,
  createPurchaseOrder,
  getPurchaseOrder,
  listPurchaseOrders,
  receivePurchaseOrder,
  submitPurchaseOrder,
} from "../api/purchaseOrderApi";
import { searchItems } from "../api/itemApi";
import { getSuppliers } from "../api/supplierApi";
import { getWarehouses } from "../api/warehouseApi";
import type { PurchaseOrder } from "../types/purchaseOrder";
import type { Item } from "../types/item";
import type { Supplier } from "../types/supplier";
import type { Warehouse } from "../types/warehouse";

/**
 * STUDY: Purchase Order CRM UI — create DRAFT → submit → receive into stock.
 */
export default function PurchaseOrderPage() {
  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [selected, setSelected] = useState<PurchaseOrder | null>(null);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [items, setItems] = useState<Item[]>([]);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [form, setForm] = useState({
    poNumber: "",
    supplierId: "",
    warehouseId: "",
    expectedAt: "",
    notes: "",
    itemId: "",
    quantityOrdered: 10,
    unitCost: 0,
  });

  async function reload() {
    const list = await listPurchaseOrders();
    setOrders(list);
  }

  useEffect(() => {
    void getSuppliers().then((s) => {
      setSuppliers(s);
      if (s[0]) setForm((f) => ({ ...f, supplierId: s[0].id }));
    });
    void getWarehouses().then((w) => {
      setWarehouses(w);
      if (w[0]) setForm((f) => ({ ...f, warehouseId: w[0].id }));
    });
    void searchItems({ page: 0, size: 100 }).then((p) => {
      setItems(p.content);
      if (p.content[0]) setForm((f) => ({ ...f, itemId: p.content[0].id }));
    });
    void reload().catch((err) =>
      setError(err instanceof Error ? err.message : "Load failed")
    );
  }, []);

  async function openDetail(id: string) {
    try {
      setError("");
      setSelected(await getPurchaseOrder(id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Load detail failed");
    }
  }

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    try {
      setError("");
      const created = await createPurchaseOrder({
        poNumber: form.poNumber,
        supplierId: form.supplierId,
        warehouseId: form.warehouseId,
        expectedAt: form.expectedAt || undefined,
        notes: form.notes || undefined,
        lines: [
          {
            itemId: form.itemId,
            quantityOrdered: form.quantityOrdered,
            unitCost: form.unitCost || undefined,
          },
        ],
      });
      setInfo(`Created ${created.poNumber}`);
      setForm((f) => ({ ...f, poNumber: "" }));
      await reload();
      setSelected(created);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Create failed");
    }
  }

  async function onSubmitPo() {
    if (!selected) return;
    try {
      setSelected(await submitPurchaseOrder(selected.id));
      setInfo("Submitted (ORDERED)");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submit failed");
    }
  }

  async function onCancelPo() {
    if (!selected) return;
    try {
      setSelected(await cancelPurchaseOrder(selected.id));
      setInfo("Cancelled");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Cancel failed");
    }
  }

  async function onReceiveRemaining() {
    if (!selected) return;
    const lines = selected.lines
      .filter((l) => l.remaining > 0)
      .map((l) => ({ lineId: l.id, quantity: l.remaining }));
    if (lines.length === 0) {
      setInfo("Nothing left to receive");
      return;
    }
    try {
      setSelected(await receivePurchaseOrder(selected.id, lines));
      setInfo("Received remaining qty into warehouse stock");
      await reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Receive failed");
    }
  }

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <h1>Purchase Orders</h1>
          <p>
            Inbound CRM: DRAFT → submit → receive goods (increases inventory + movements).
          </p>
        </div>
      </header>

      {error && <p className="error">{error}</p>}
      {info && <p className="info">{info}</p>}

      <form className="inline-form crm-panel" onSubmit={onCreate}>
        <span className="crm-label">New DRAFT</span>
        <input
          required
          placeholder="PO-2026-xxxx"
          value={form.poNumber}
          onChange={(e) => setForm({ ...form, poNumber: e.target.value })}
        />
        <select
          value={form.supplierId}
          onChange={(e) => setForm({ ...form, supplierId: e.target.value })}
        >
          {suppliers.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
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
          value={form.quantityOrdered}
          onChange={(e) => setForm({ ...form, quantityOrdered: Number(e.target.value) })}
        />
        <button type="submit">Create PO</button>
      </form>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>PO #</th>
              <th>Supplier</th>
              <th>Warehouse</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.id}>
                <td>{o.poNumber}</td>
                <td>{o.supplierName}</td>
                <td>{o.warehouseName}</td>
                <td>{o.status}</td>
                <td>
                  <button type="button" className="btn-secondary" onClick={() => void openDetail(o.id)}>
                    Open
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selected && (
        <section className="crm-panel" style={{ marginTop: 16 }}>
          <h2>
            {selected.poNumber} — {selected.status}
          </h2>
          <p>
            {selected.supplierName} → {selected.warehouseName}
          </p>
          <div className="filters">
            {selected.status === "DRAFT" && (
              <button type="button" onClick={() => void onSubmitPo()}>
                Submit
              </button>
            )}
            {(selected.status === "ORDERED" || selected.status === "PARTIAL") && (
              <button type="button" onClick={() => void onReceiveRemaining()}>
                Receive remaining
              </button>
            )}
            {selected.status !== "RECEIVED" && selected.status !== "CANCELLED" && (
              <button type="button" className="btn-secondary" onClick={() => void onCancelPo()}>
                Cancel
              </button>
            )}
          </div>
          <table>
            <thead>
              <tr>
                <th>Item</th>
                <th>Ordered</th>
                <th>Received</th>
                <th>Remaining</th>
              </tr>
            </thead>
            <tbody>
              {selected.lines.map((l) => (
                <tr key={l.id}>
                  <td>{l.itemName}</td>
                  <td>{l.quantityOrdered}</td>
                  <td>{l.quantityReceived}</td>
                  <td>{l.remaining}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}
    </main>
  );
}
