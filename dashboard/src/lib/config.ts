export const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8080";

// Default true: the dashboard is fully demoable with zero backend running.
// Set VITE_USE_MOCKS=false once irp-core (and the gap-fill endpoints noted in
// the Phase 4 design) are available.
export const USE_MOCKS: boolean =
  (import.meta.env.VITE_USE_MOCKS as string | undefined) !== "false";
