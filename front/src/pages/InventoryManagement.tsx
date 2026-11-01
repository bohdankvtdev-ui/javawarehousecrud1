import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  adjustInventory,
  deleteInventory,
  getInventory,
  getInventoryByWarehouse,
  getLowStock,
  getRecentMovements,
  getReorderSuggestions,
  getStockReport,
  transferStock,
  upsertInventory,
} from "../api/inventoryApi";
import { searchItems } from "../api/itemApi";
import { getWarehouses } from "../api/warehouseApi";
import LoadingBlock from "../components/LoadingBlock";
import type {
  Inventory,
  InventoryRequest,
  InventoryStockReport,
  ReorderSuggestion,
  StockMovement,
} from "../types/inventory";
import type { Item } from "../types/item";
import type { Warehouse } from "../types/warehouse";

type ViewMode = "all" | "warehouse" | "low" | "jdbc" | "reorder" | "movements";

/**
 * STUDY: Warehouse CRM stock UI.
 * Upsert + adjust (receive/ship) + transfer + reorder + movement audit.
 */
export default function InventoryManagement() {
  const [rows, setRows] = useState<Inventory[]>([]);
  const [report, setReport] = useState<InventoryStockReport[]>([]);
  const [reorders, setReorders] = useState<ReorderSuggestion[]>([]);
  const [movements, setMovements] = useState<StockMovement[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [items, setItems] = useState<Item[]>([]);
  const [warehouseId, setWarehouseId] = useState("");
  const [mode, setMode] = useState<ViewMode>("all");
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [loading, setLoading] = useState(true);
  const [adjustId, setAdjustId] = useState<string | null>(null);
  const [adjustDelta, setAdjustDelta] = useState(1);
  const [adjustReason, setAdjustReason] = useState("Receive goods");
  const [transfer, setTransfer] = useState({
    fromWarehouseId: "",
    toWarehouseId: "",
    itemId: "",
    quantity: 1,
    reason: "Inter-warehouse transfer",
  });
  const [form, setForm] = useState<InventoryRequest>({
    warehouseId: "",
    itemId: "",
    quantity: 0,
    minQuantity: 0,
    location: "",
    warehouseSection: "",
  });

  useEffect(() => {
    void getWarehouses().then((data) => {
      setWarehouses(data);
      if (data[0]) {
        setWarehouseId(data[0].id);
        setForm((f) => ({ ...f, warehouseId: data[0].id }));
        setTransfer((t) => ({
          ...t,
          fromWarehouseId: data[0].id,
          toWarehouseId: data[1]?.id ?? data[0].id,
        }));
      }
    });
    void searchItems({ page: 0, size: 100 }).then((page) => {
      setItems(page.content);
      if (page.content[0]) {
        setForm((f) => ({ ...f, itemId: page.content[0].id }));
        setTransfer((t) => ({ ...t, itemId: page.content[0].id }));
      }
    });
  }, []);

  async function load() {
    try {
      setLoading(true);
      setError("");
      if (mode === "jdbc") {
        setReport(await getStockReport());
        setRows([]);
        setReorders([]);
        setMovements([]);
        return;
      }
      if (mode === "reorder") {
        setReorders(await getReorderSuggestions());
        setRows([]);
        setReport([]);
        setMovements([]);
        return;
      }
      if (mode === "movements") {
        setMovements(await getRecentMovements(30));
        setRows([]);
        setReport([]);
        setReorders([]);
        return;
      }
      if (mode === "warehouse") {
        setRows(await getInventoryByWarehouse(warehouseId));
      } else if (mode === "low") {
        setRows(await getLowStock(warehouseId));
      } else {
        setRows(await getInventory());
      }
      setReport([]);
      setReorders([]);
      setMovements([]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Load failed");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (mode !== "all" && mode !== "jdbc" && mode !== "reorder" && mode !== "movements" && !warehouseId) {
      return;
    }
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode, warehouseId]);

  const existingForPair = useMemo(
    () => rows.find((r) => r.warehouseId === form.warehouseId && r.itemId === form.itemId),
    [rows, form.warehouseId, form.itemId]
  );

  useEffect(() => {
    if (!existingForPair) return;
    setForm((f) => ({
      ...f,
      quantity: existingForPair.quantity,
      minQuantity: existingForPair.minQuantity,
      location: existingForPair.location ?? "",
      warehouseSection: existingForPair.warehouseSection ?? "",
    }));
    setInfo(
      `This warehouse already has “${existingForPair.itemName}”. Saving will UPDATE that row.`
    );
  }, [existingForPair]);

  function clearFormExtras() {
    setForm((f) => ({
      ...f,
      quantity: 0,
      minQuantity: 0,
      location: "",
      warehouseSection: "",
    }));
    setInfo("");
  }

  function startEdit(row: Inventory) {
    setForm({
      warehouseId: row.warehouseId,
      itemId: row.itemId,
      quantity: row.quantity,
      minQuantity: row.minQuantity,
      location: row.location ?? "",
      warehouseSection: row.warehouseSection ?? "",
    });
    setInfo(`Editing stock for ${row.itemName} @ ${row.warehouseName}`);
    setError("");
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      setError("");
      const saved = await upsertInventory(form);
      setInfo(
        existingForPair
          ? `Updated stock for ${saved.itemName}.`
          : `Created stock for ${saved.itemName}.`
      );
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  }

  async function onDelete(id: string) {
    if (!window.confirm("Delete inventory row? (needs admin/admin)")) return;
    try {
      await deleteInventory(id);
      setInfo("Stock row deleted.");
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  async function onAdjust(e: FormEvent) {
    e.preventDefault();
    if (!adjustId) return;
    try {
      setError("");
      const saved = await adjustInventory(adjustId, {
        delta: adjustDelta,
        reason: adjustReason,
      });
      setInfo(
        `Adjusted ${saved.itemName}: now ${saved.quantity} (${adjustDelta > 0 ? "+" : ""}${adjustDelta})`
      );
      setAdjustId(null);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Adjust failed");
    }
  }

  async function onTransfer(e: FormEvent) {
    e.preventDefault();
    try {
      setError("");
      const [from, to] = await transferStock(transfer);
      setInfo(
        `Transferred ${transfer.quantity} of ${from.itemName}: ${from.warehouseName} → ${to.warehouseName}`
      );
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Transfer failed");
    }
  }

  const showStockTable =
    mode === "all" || mode === "warehouse" || mode === "low";

  return (
    <main className="item-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Stock · /api/v1/inventory</p>
          <h1>Inventory</h1>
          <p>
            On-hand qty lives here (not on Items). Use <strong>adjust</strong>,{" "}
            <strong>transfer</strong>, reorder list, and movement history to mirror backend CRM.
          </p>
        </div>
      </header>

      <p className="hint-block" style={{ marginTop: 0 }}>
        <strong>Ver</strong> = JPA <code>@Version</code> (optimistic lock). Seed rows start at{" "}
        <code>0</code>; it increments only after an update (Adjust / Transfer / Receive). Click
        Adjust on a row — that Ver should become <code>1</code>.
      </p>

      {error && <p className="error">{error}</p>}
      {info && <p className="info">{info}</p>}
      {loading && rows.length === 0 && report.length === 0 && reorders.length === 0 && movements.length === 0 && (
        <LoadingBlock label="Loading stock…" />
      )}

      <div className="filters">
        <select value={mode} onChange={(e) => setMode(e.target.value as ViewMode)}>
          <option value="all">All stock (JPA)</option>
          <option value="warehouse">By warehouse (JPA)</option>
          <option value="low">Low stock (JPA)</option>
          <option value="reorder">Reorder suggestions (CRM)</option>
          <option value="movements">Movement history (CRM)</option>
          <option value="jdbc">Stock report (JdbcTemplate)</option>
        </select>
        {(mode === "warehouse" || mode === "low") && (
          <select value={warehouseId} onChange={(e) => setWarehouseId(e.target.value)}>
            {warehouses.map((w) => (
              <option key={w.id} value={w.id}>
                {w.name}
              </option>
            ))}
          </select>
        )}
        <button type="button" className="btn-secondary" onClick={() => void load()}>
          Refresh
        </button>
      </div>

      {showStockTable && (
        <>
          <form className="inline-form" onSubmit={onSubmit}>
            <select
              required
              value={form.warehouseId}
              onChange={(e) => {
                setForm({ ...form, warehouseId: e.target.value });
                setInfo("");
              }}
            >
              {warehouses.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
            <select
              required
              value={form.itemId}
              onChange={(e) => {
                setForm({ ...form, itemId: e.target.value });
                setInfo("");
              }}
            >
              {items.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.itemName}
                </option>
              ))}
            </select>
            <input
              type="number"
              min={0}
              placeholder="Qty"
              value={form.quantity}
              onChange={(e) => setForm({ ...form, quantity: Number(e.target.value) })}
            />
            <input
              type="number"
              min={0}
              placeholder="Min qty"
              value={form.minQuantity}
              onChange={(e) => setForm({ ...form, minQuantity: Number(e.target.value) })}
            />
            <input
              placeholder="Location"
              value={form.location}
              onChange={(e) => setForm({ ...form, location: e.target.value })}
            />
            <input
              placeholder="Section"
              value={form.warehouseSection}
              onChange={(e) => setForm({ ...form, warehouseSection: e.target.value })}
            />
            <button type="submit">
              {existingForPair ? "Update stock" : "Save stock"}
            </button>
            <button type="button" className="btn-secondary" onClick={clearFormExtras}>
              Clear qty
            </button>
          </form>

          <form className="inline-form crm-panel" onSubmit={onTransfer}>
            <span className="crm-label">Transfer</span>
            <select
              required
              value={transfer.fromWarehouseId}
              onChange={(e) => setTransfer({ ...transfer, fromWarehouseId: e.target.value })}
            >
              {warehouses.map((w) => (
                <option key={w.id} value={w.id}>
                  From: {w.name}
                </option>
              ))}
            </select>
            <select
              required
              value={transfer.toWarehouseId}
              onChange={(e) => setTransfer({ ...transfer, toWarehouseId: e.target.value })}
            >
              {warehouses.map((w) => (
                <option key={w.id} value={w.id}>
                  To: {w.name}
                </option>
              ))}
            </select>
            <select
              required
              value={transfer.itemId}
              onChange={(e) => setTransfer({ ...transfer, itemId: e.target.value })}
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
              value={transfer.quantity}
              onChange={(e) =>
                setTransfer({ ...transfer, quantity: Number(e.target.value) })
              }
            />
            <input
              placeholder="Reason"
              value={transfer.reason}
              onChange={(e) => setTransfer({ ...transfer, reason: e.target.value })}
            />
            <button type="submit">Transfer</button>
          </form>
        </>
      )}

      {adjustId && (
        <form className="inline-form crm-panel" onSubmit={onAdjust}>
          <span className="crm-label">Adjust ±qty</span>
          <input
            type="number"
            value={adjustDelta}
            onChange={(e) => setAdjustDelta(Number(e.target.value))}
          />
          <input
            placeholder="Reason"
            value={adjustReason}
            onChange={(e) => setAdjustReason(e.target.value)}
            required
          />
          <button type="submit">Apply adjust</button>
          <button type="button" className="btn-secondary" onClick={() => setAdjustId(null)}>
            Cancel
          </button>
        </form>
      )}

      <div className={`table-container ${loading ? "table-dim" : ""}`}>
        {mode === "jdbc" && (
          <table>
            <thead>
              <tr>
                <th>Warehouse</th>
                <th>Item</th>
                <th>Qty</th>
                <th>Min</th>
                <th>Low?</th>
              </tr>
            </thead>
            <tbody>
              {report.map((row) => (
                <tr key={row.inventoryId}>
                  <td>{row.warehouseName}</td>
                  <td>{row.itemName}</td>
                  <td>{row.quantity}</td>
                  <td>{row.minQuantity}</td>
                  <td>
                    {row.lowStock ? (
                      <span className="badge-low">LOW</span>
                    ) : (
                      <span className="badge-ok">OK</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {mode === "reorder" && (
          <table>
            <thead>
              <tr>
                <th>Warehouse</th>
                <th>Item</th>
                <th>Qty</th>
                <th>Min</th>
                <th>Shortage</th>
                <th>Supplier</th>
              </tr>
            </thead>
            <tbody>
              {reorders.map((row) => (
                <tr key={row.inventoryId}>
                  <td>{row.warehouseName}</td>
                  <td>{row.itemName}</td>
                  <td>{row.quantity}</td>
                  <td>{row.minQuantity}</td>
                  <td>{row.shortage}</td>
                  <td>{row.supplierName}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {mode === "movements" && (
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Type</th>
                <th>Warehouse</th>
                <th>Item</th>
                <th>Δ</th>
                <th>After</th>
                <th>Reason</th>
                <th>Related WH</th>
              </tr>
            </thead>
            <tbody>
              {movements.map((m) => (
                <tr key={m.id}>
                  <td>{new Date(m.createdAt).toLocaleString()}</td>
                  <td>{m.movementType}</td>
                  <td>{m.warehouseName}</td>
                  <td>{m.itemName}</td>
                  <td>{m.quantityDelta > 0 ? `+${m.quantityDelta}` : m.quantityDelta}</td>
                  <td>{m.quantityAfter}</td>
                  <td>{m.reason}</td>
                  <td>{m.relatedWarehouseName ?? "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {showStockTable && (
          <table>
            <thead>
              <tr>
                <th>Warehouse</th>
                <th>Item</th>
                <th>On hand</th>
                <th>Reserved</th>
                <th>Available</th>
                <th>Min</th>
                <th title="Optimistic lock (@Version). 0 until first update.">Ver</th>
                <th>Location</th>
                <th>Low?</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.warehouseName}</td>
                  <td>{row.itemName}</td>
                  <td>{row.quantity}</td>
                  <td>{row.reservedQuantity}</td>
                  <td>{row.availableQuantity}</td>
                  <td>{row.minQuantity}</td>
                  <td>{row.version}</td>
                  <td>
                    {row.location} {row.warehouseSection}
                  </td>
                  <td>
                    {row.lowStock ? (
                      <span className="badge-low">LOW</span>
                    ) : (
                      <span className="badge-ok">OK</span>
                    )}
                  </td>
                  <td className="actions">
                    <button type="button" className="btn-secondary" onClick={() => startEdit(row)}>
                      Edit
                    </button>
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => {
                        setAdjustId(row.id);
                        setAdjustDelta(1);
                        setAdjustReason("Receive goods");
                      }}
                    >
                      Adjust
                    </button>
                    <button type="button" onClick={() => onDelete(row.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </main>
  );
}
