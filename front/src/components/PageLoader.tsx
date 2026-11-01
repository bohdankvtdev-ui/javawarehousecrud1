/**
 * STUDY: Suspense / route fallback — lightweight spinner instead of blank screen.
 */
export default function PageLoader({ label = "Loading…" }: { label?: string }) {
  return (
    <div className="page-loader" role="status" aria-live="polite">
      <div className="spinner" aria-hidden />
      <span>{label}</span>
    </div>
  );
}
