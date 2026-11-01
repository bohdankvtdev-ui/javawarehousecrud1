export interface Supplier {
  id: string;
  name: string;
  email: string | null;
  phone: string | null;
}

export interface SupplierRequest {
  name: string;
  email: string;
  phone: string;
}
