"use client";
import { useDeferredValue, useMemo, useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import {
  Search,
  Bookmark,
  X,
  ArrowRight,
  SlidersHorizontal,
} from "lucide-react";
import { SchemeCard } from "@/features/schemes/components/SchemeCard";
import { SchemeSkeleton } from "@/features/schemes/components/SchemeSkeleton";
import { useSchemes } from "@/features/schemes/hooks/useSchemes";
import { useSavedSchemeIds } from "@/features/bookmarks/hooks";
import { toast } from "sonner";
export default function SchemesPage() {
  return (
    <Suspense fallback={<SchemeSkeleton />}>
      <Catalogue />
    </Suspense>
  );
}
function Catalogue() {
  const query = useSchemes();
  const data = query.data;
  const savedQuery = useSavedSchemeIds();
  const saved = useMemo(() => savedQuery.data ?? [], [savedQuery.data]);
  const params = useSearchParams();
  const [search, setSearch] = useState("");
  const deferred = useDeferredValue(search);
  const [state, setState] = useState("ALL");
  const [sort, setSort] = useState("name");
  const [onlySaved, setOnlySaved] = useState(params.get("saved") === "1");
  const [compare, setCompare] = useState<string[]>([]);
  const states = useMemo(
    () =>
      [...new Set(data?.map((s) => s.state) ?? [])]
        .filter((s) => s !== "India" && s !== "Central")
        .sort(),
    [data],
  );
  const filtered = useMemo(
    () =>
      (data ?? [])
        .filter(
          (s) =>
            (!onlySaved || saved.includes(s.id)) &&
            (state === "ALL" ||
              [state, "India", "Central"].includes(s.state)) &&
            [s.name, s.description, s.department, ...s.benefits]
              .join(" ")
              .toLowerCase()
              .includes(deferred.trim().toLowerCase()),
        )
        .sort((a, b) =>
          sort === "name"
            ? a.name.localeCompare(b.name)
            : (b.lastVerifiedAt ?? "").localeCompare(a.lastVerifiedAt ?? ""),
        ),
    [data, onlySaved, saved, state, deferred, sort],
  );
  function toggle(id: string) {
    if (compare.includes(id)) setCompare(compare.filter((v) => v !== id));
    else if (compare.length < 3) setCompare([...compare, id]);
    else toast.info("Compare up to three schemes at a time.");
  }
  return (
    <div className="space-y-7 pb-24">
      <div>
        <h1 className="editorial text-4xl sm:text-5xl">
          Find support that fits your life.
        </h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Explore the catalogue, understand the requirements, and make an
          informed next move.
        </p>
      </div>
      <div className="space-y-4 rounded-xl border bg-white p-5">
        <div className="flex items-center gap-3 rounded-lg border bg-background px-4">
          <Search size={19} className="text-muted-foreground" />
          <input
            aria-label="Search schemes"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by scheme, benefit, or department…"
            className="h-12 min-w-0 flex-1 bg-transparent text-sm outline-none"
          />
          {search && (
            <button aria-label="Clear search" onClick={() => setSearch("")}>
              <X size={17} />
            </button>
          )}
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <SlidersHorizontal size={16} className="text-muted-foreground" />
          <select
            aria-label="Filter by state"
            value={state}
            onChange={(e) => setState(e.target.value)}
            className="min-h-10 max-w-full rounded-lg border bg-white px-3 text-sm"
          >
            <option value="ALL">All states</option>
            {states.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
          <select
            aria-label="Sort schemes"
            value={sort}
            onChange={(e) => setSort(e.target.value)}
            className="min-h-10 rounded-lg border bg-white px-3 text-sm"
          >
            <option value="name">Name: A to Z</option>
            <option value="verified">Recently verified</option>
          </select>
          <button
            onClick={() => setOnlySaved(!onlySaved)}
            aria-pressed={onlySaved}
            className={`flex min-h-10 items-center gap-2 rounded-lg border px-3 text-sm ${onlySaved ? "border-primary bg-accent text-primary" : "hover:bg-muted"}`}
          >
            <Bookmark size={15} />
            Saved ({savedQuery.isError ? "—" : saved.length})
          </button>
          <span
            aria-live="polite"
            className="ml-auto text-xs text-muted-foreground"
          >
            {query.isPending ? "Loading…" : `${filtered.length} schemes`}
          </span>
        </div>
        {savedQuery.isError && (
          <p role="alert" className="text-xs text-destructive">
            Saved schemes are unavailable. <button className="text-link" onClick={() => savedQuery.refetch()}>Retry</button>
          </p>
        )}
        {onlySaved && !savedQuery.isError && (
          <p className="text-xs text-muted-foreground">
            Your shortlist is saved for this account on this device.
          </p>
        )}
      </div>
      {query.isPending || (onlySaved && savedQuery.isPending) ? (
        <SchemeSkeleton />
      ) : query.isError || (onlySaved && savedQuery.isError) ? (
        <div role="alert" className="paper-card p-8">
          <p>We couldn’t load the catalogue.</p>
          <button className="text-link mt-3" onClick={() => query.refetch()}>
            Try again
          </button>
        </div>
      ) : filtered.length === 0 ? (
        <div className="rounded-xl border bg-white px-6 py-16 text-center">
          <Search className="mx-auto mb-4 text-primary" />
          <h2 className="editorial text-2xl">
            {onlySaved
              ? "No saved schemes here yet"
              : "No schemes match these filters"}
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Try a broader search or explore all available schemes.
          </p>
          <button
            className="text-link mt-5"
            onClick={() => {
              setSearch("");
              setState("ALL");
              setOnlySaved(false);
            }}
          >
            Reset filters <ArrowRight size={15} />
          </button>
        </div>
      ) : (
        <div className="grid gap-5 md:grid-cols-2 2xl:grid-cols-3">
          {filtered.map((s) => (
            <SchemeCard
              key={s.id}
              scheme={s}
              compared={compare.includes(s.id)}
              onCompare={() => toggle(s.id)}
            />
          ))}
        </div>
      )}
      {compare.length > 0 && (
        <div className="fixed bottom-5 left-5 right-5 z-20 mx-auto flex max-w-2xl flex-wrap items-center gap-3 rounded-xl border bg-white p-4 shadow-lg lg:left-[268px]">
          <p className="flex-1 text-sm font-medium">
            {compare.length} of 3 selected
          </p>
          <button
            className="text-xs text-muted-foreground"
            onClick={() => setCompare([])}
          >
            Clear
          </button>
          <Link
            aria-disabled={compare.length < 2}
            onClick={(e) => {
              if (compare.length < 2) e.preventDefault();
            }}
            href={`/schemes/compare?ids=${compare.join(",")}`}
            className={`action-link ${compare.length < 2 ? "opacity-50" : ""}`}
          >
            Compare schemes <ArrowRight size={16} />
          </Link>
        </div>
      )}
    </div>
  );
}
