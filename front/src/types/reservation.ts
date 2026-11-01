export type ReservationStatus = "ACTIVE" | "RELEASED" | "FULFILLED";

export interface Reservation {
  id: string;
  warehouseId: string;
  warehouseName: string;
  itemId: string;
  itemName: string;
  quantity: number;
  reference: string;
  status: ReservationStatus;
  notes: string | null;
  createdAt: string;
}

export interface ReservationRequest {
  warehouseId: string;
  itemId: string;
  quantity: number;
  reference: string;
  notes?: string;
}
