import type { Item, ItemStatus } from "../types/item";

interface Props {
  items: Item[];
  onEdit: (item: Item) => void;
  onDelete: (id: string) => void;
  onStatusChange?: (id: string, status: ItemStatus) => void;
}

export default function ItemTable({ items, onEdit, onDelete, onStatusChange }: Props) {
  return (
    <div className="table-container">
      <table>
        <thead>
          <tr>
            <th>Item</th>
            <th>Type</th>
            <th>Category</th>
            <th>Supplier</th>
            <th>Price</th>
            <th>Weight</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.id}>
              <td>{item.itemName}</td>
              <td>{item.type}</td>
              <td>{item.categoryName}</td>
              <td>{item.supplierName}</td>
              <td>{Number(item.price).toFixed(2)}</td>
              <td>{item.weight != null ? `${item.weight} kg` : "—"}</td>
              <td>
                {onStatusChange ? (
                  <select
                    className={`status-select ${item.status.toLowerCase()}`}
                    value={item.status}
                    onChange={(e) =>
                      onStatusChange(item.id, e.target.value as ItemStatus)
                    }
                    aria-label={`Status for ${item.itemName}`}
                  >
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                    <option value="DISCONTINUED">DISCONTINUED</option>
                  </select>
                ) : (
                  <span className={`status ${item.status.toLowerCase()}`}>
                    {item.status}
                  </span>
                )}
              </td>
              <td className="actions">
                <button type="button" className="btn-secondary" onClick={() => onEdit(item)}>
                  Edit
                </button>
                <button type="button" onClick={() => onDelete(item.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {items.length === 0 && <p className="empty">No items found.</p>}
    </div>
  );
}
