import { getAuthHeader } from "./auth";

/**
 * STUDY: Central API client.
 * - Relative `/api/...` → Vite proxy → Spring Boot :8080
 * - Attaches Basic Auth header when logged in (needed for POST/PUT/DELETE)
 */
export const API_BASE =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ?? "";

export async function apiFetch<T>(
  path: string,
  init?: RequestInit
): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeader(),
      ...(init?.headers ?? {}),
    },
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    if (response.status === 401) {
      message = "Unauthorized — log in as user/user or admin/admin";
    } else if (response.status === 403) {
      message = "Forbidden — need ADMIN role for delete (login as admin/admin)";
    } else {
      try {
        const body = await response.json();
        if (body?.message) {
          message = body.message;
          if (Array.isArray(body.details) && body.details.length > 0) {
            message += ": " + body.details.join("; ");
          }
        }
      } catch {
        // ignore
      }
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
