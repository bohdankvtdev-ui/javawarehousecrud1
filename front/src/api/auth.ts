/**
 * STUDY: Simple client-side auth for HTTP Basic.
 * Production SPAs usually use JWT in memory / httpOnly cookies instead.
 */

const KEY = "warehouse.basicAuth";

export function setAuth(username: string, password: string): void {
  const token = btoa(`${username}:${password}`);
  sessionStorage.setItem(KEY, token);
}

export function clearAuth(): void {
  sessionStorage.removeItem(KEY);
}

export function getAuthHeader(): Record<string, string> {
  const token = sessionStorage.getItem(KEY);
  return token ? { Authorization: `Basic ${token}` } : {};
}

export function isLoggedIn(): boolean {
  return sessionStorage.getItem(KEY) != null;
}
