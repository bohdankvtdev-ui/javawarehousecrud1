import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

/**
 * STUDY: Vite proxy avoids CORS during local dev if you call `/api/...`
 * relative URLs. We also allow absolute VITE_API_BASE_URL for clarity.
 *
 * Alternative: only CORS on the Spring side (already configured) and call
 * http://localhost:8080 directly from the browser.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
