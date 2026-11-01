import { useEffect, useState, type FormEvent } from "react";
import type { Category } from "../types/category";
import type { Item, ItemRequest } from "../types/item";
import type { Supplier } from "../types/supplier";

interface Props {
  categories: Category[];
  suppliers: Supplier[];
  initial?: Item | null;
  onSubmit: (data: ItemRequest) => Promise<void>;
  onCancel: () => void;
}

const empty: ItemRequest = {
  itemName: "",
  type: "",
  description: "",
  categoryId: "",
  supplierId: "",
  price: 0,
  weight: 0,
};

export default function ItemForm({
  categories,
  suppliers,
  initial,
  onSubmit,
  onCancel,
}: Props) {
  const [form, setForm] = useState<ItemRequest>(empty);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (initial) {
      setForm({
        itemName: initial.itemName,
        type: initial.type ?? "",
        description: initial.description ?? "",
        categoryId: initial.categoryId,
        supplierId: initial.supplierId,
        price: initial.price,
        weight: initial.weight ?? 0,
      });
    } else {
      setForm({
        ...empty,
        categoryId: categories[0]?.id ?? "",
        supplierId: suppliers[0]?.id ?? "",
      });
    }
  }, [initial, categories, suppliers]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      await onSubmit({
        ...form,
        price: Number(form.price),
        weight: form.weight === null || form.weight === undefined
          ? null
          : Number(form.weight),
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="modal-backdrop">
      <form className="modal" onSubmit={handleSubmit}>
        <h2>{initial ? "Edit warehouse item" : "Add warehouse item"}</h2>

        <label>
          Item name
          <input
            required
            value={form.itemName}
            onChange={(e) => setForm({ ...form, itemName: e.target.value })}
          />
        </label>

        <label>
          Type
          <input
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          />
        </label>

        <label>
          Description
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </label>

        <label>
          Category
          <select
            required
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
          >
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Supplier
          <select
            required
            value={form.supplierId}
            onChange={(e) => setForm({ ...form, supplierId: e.target.value })}
          >
            {suppliers.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>

        <div className="form-row">
          <label>
            Price
            <input
              type="number"
              min="0"
              step="0.01"
              required
              value={form.price}
              onChange={(e) =>
                setForm({ ...form, price: Number(e.target.value) })
              }
            />
          </label>
          <label>
            Weight (kg)
            <input
              type="number"
              min="0"
              step="0.01"
              value={form.weight ?? 0}
              onChange={(e) =>
                setForm({ ...form, weight: Number(e.target.value) })
              }
            />
          </label>
        </div>

        {error && <p className="error">{error}</p>}

        <div className="form-actions">
          <button type="button" className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
          <button type="submit" disabled={saving}>
            {saving ? "Saving…" : "Save item"}
          </button>
        </div>
      </form>
    </div>
  );
}
