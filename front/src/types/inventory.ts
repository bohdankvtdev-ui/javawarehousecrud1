export interface Inventory {
  id: string;
  warehouseId: string;
  warehouseName: string;
  itemId: string;
  itemName: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  minQuantity: number;
  version: number;
  location: string | null;
  warehouseSection: string | null;
  updatedAt: string;
  lowStock: boolean;
}

export interface InventoryRequest {
  warehouseId: string;
  itemId: string;
  quantity: number;
  minQuantity: number;
  location: string;
  warehouseSection: string;
}

export interface InventoryStockReport {
  inventoryId: string;
  warehouseName: string;
  itemName: string;
  quantity: number;
  minQuantity: number;
  lowStock: boolean;
}

export interface StockAdjustRequest {
  delta: number;
  reason: string;
}

export interface StockTransferRequest {
  fromWarehouseId: string;
  toWarehouseId: string;
  itemId: string;
  quantity: number;
  reason: string;
}

export interface StockMovement {
  id: string;
  warehouseId: string;
  warehouseName: string;
  itemId: string;
  itemName: string;
  movementType: string;
  quantityDelta: number;
  quantityAfter: number;
  reason: string | null;
  relatedWarehouseId: string | null;
  relatedWarehouseName: string | null;
  createdAt: string;
}

export interface ReorderSuggestion {
  inventoryId: string;
  warehouseId: string;
  warehouseName: string;
  itemId: string;
  itemName: string;
  quantity: number;
  minQuantity: number;
  shortage: number;
  supplierName: string;
}
