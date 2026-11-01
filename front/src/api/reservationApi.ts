import { apiFetch } from "./client";
import type { Reservation, ReservationRequest, ReservationStatus } from "../types/reservation";

const BASE = "/api/v1/reservations";

export function listReservations(status?: ReservationStatus): Promise<Reservation[]> {
  const q = status ? `?status=${status}` : "";
  return apiFetch<Reservation[]>(`${BASE}${q}`);
}

export function createReservation(body: ReservationRequest): Promise<Reservation> {
  return apiFetch<Reservation>(BASE, { method: "POST", body: JSON.stringify(body) });
}

export function releaseReservation(id: string): Promise<Reservation> {
  return apiFetch<Reservation>(`${BASE}/${id}/release`, { method: "POST" });
}

export function fulfillReservation(id: string): Promise<Reservation> {
  return apiFetch<Reservation>(`${BASE}/${id}/fulfill`, { method: "POST" });
}
