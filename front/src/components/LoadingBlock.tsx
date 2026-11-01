import PageLoader from "./PageLoader";

/** Inline loading state for data fetches inside a page. */
export default function LoadingBlock({ label = "Loading data…" }: { label?: string }) {
  return (
    <div className="loading-block">
      <PageLoader label={label} />
    </div>
  );
}
