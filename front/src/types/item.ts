export type ItemStatus = "ACTIVE" | "INACTIVE" | "DISCONTINUED";

export interface Item {
  id: string;
  itemName: string;
  type: string;
  description: string;
  categoryId: string;
  categoryName: string;
  supplierId: string;
  supplierName: string;
  price: number;
  weight: number;
  status: ItemStatus;
}

export interface ItemRequest {
  itemName: string;
  type: string;
  description: string;
  categoryId: string;
  supplierId: string;
  price: number;
  weight: number | null;
}

/** Matches backend PageResponse<T> */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ItemSearchParams {
  q?: string;
  status?: ItemStatus | "";
  categoryId?: string;
  page?: number;
  size?: number;
  sort?: string;
}
