export function SchemeSkeleton() {
  return (
    <div role="status" aria-live="polite">
      <span className="sr-only">Loading schemes…</span>

      <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3" aria-hidden="true">
        {Array.from({ length: 6 }).map((_, index) => (
          <div
            key={index}
            className="flex h-full flex-col gap-4 rounded-xl border border-border bg-card p-6"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="size-11 animate-pulse rounded-xl bg-muted" />
              <div className="h-6 w-24 animate-pulse rounded-full bg-muted" />
            </div>

            <div className="space-y-2">
              <div className="h-5 w-4/5 animate-pulse rounded-md bg-muted" />
              <div className="h-5 w-1/2 animate-pulse rounded-md bg-muted" />
            </div>

            <div className="space-y-2">
              <div className="h-3.5 w-full animate-pulse rounded-md bg-muted" />
              <div className="h-3.5 w-full animate-pulse rounded-md bg-muted" />
              <div className="h-3.5 w-2/3 animate-pulse rounded-md bg-muted" />
            </div>

            <div className="flex flex-wrap gap-2">
              <div className="h-6 w-20 animate-pulse rounded-full bg-muted" />
              <div className="h-6 w-24 animate-pulse rounded-full bg-muted" />
              <div className="h-6 w-16 animate-pulse rounded-full bg-muted" />
            </div>

            <div className="mt-auto h-12 w-full animate-pulse rounded-xl bg-muted" />
          </div>
        ))}
      </div>
    </div>
  );
}
