export interface Warehouse {
  id: string;
  name: string;
  address: string | null;
  city: string | null;
  capacity: number | null;
  createdAt: string;
}

export interface WarehouseRequest {
  name: string;
  address: string;
  city: string;
  capacity: number | null;
}
