export type PurchaseOrderStatus =
  | "DRAFT"
  | "ORDERED"
  | "PARTIAL"
  | "RECEIVED"
  | "CANCELLED";

export interface PurchaseOrderLine {
  id: string;
  itemId: string;
  itemName: string;
  quantityOrdered: number;
  quantityReceived: number;
  remaining: number;
  unitCost: number | null;
}

export interface PurchaseOrder {
  id: string;
  poNumber: string;
  supplierId: string;
  supplierName: string;
  warehouseId: string;
  warehouseName: string;
  status: PurchaseOrderStatus;
  orderedAt: string | null;
  expectedAt: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
  lines: PurchaseOrderLine[];
}

export interface PurchaseOrderRequest {
  poNumber: string;
  supplierId: string;
  warehouseId: string;
  expectedAt?: string;
  notes?: string;
  lines: { itemId: string; quantityOrdered: number; unitCost?: number }[];
}
