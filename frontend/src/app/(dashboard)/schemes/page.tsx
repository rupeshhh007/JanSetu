"use client";

import { useMemo, useState } from "react";
import { AlertCircle, FileText } from "lucide-react";

import { EmptySchemes } from "@/features/schemes/components/EmptySchemes";
import { SchemeGrid } from "@/features/schemes/components/SchemeGrid";
import { SchemeSkeleton } from "@/features/schemes/components/SchemeSkeleton";
import { SearchBar } from "@/features/schemes/components/SearchBar";
import { useSchemes } from "@/features/schemes/hooks/useSchemes";

export default function SchemesPage() {
  const { data = [], isLoading, isError } = useSchemes();

  const [search, setSearch] = useState("");

  const filteredSchemes = useMemo(() => {
    const query = search.toLowerCase();

    return data.filter(
      (scheme) =>
        scheme.name.toLowerCase().includes(query) ||
        scheme.description.toLowerCase().includes(query)
    );
  }, [data, search]);

  return (
    <div className="space-y-10">
      {/* Page header */}
      <div className="flex items-center gap-4">
        <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500/15 via-blue-500/10 to-transparent text-primary ring-1 ring-primary/15">
          <FileText className="size-5" strokeWidth={2} aria-hidden="true" />
        </div>

        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Government Schemes
          </h1>
          <p className="mt-1 text-muted-foreground">
            Browse all available government welfare schemes.
          </p>
        </div>
      </div>

      {/* Search + result count */}
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <SearchBar value={search} onChange={setSearch} />

        {!isLoading && !isError && (
          <p className="text-sm text-muted-foreground" aria-live="polite">
            {filteredSchemes.length} {filteredSchemes.length === 1 ? "scheme" : "schemes"} found
          </p>
        )}
      </div>

      {isLoading ? (
        <SchemeSkeleton />
      ) : isError ? (
        <div
          role="alert"
          className="flex flex-col items-center gap-3 rounded-2xl border border-destructive/20 bg-destructive/5 px-6 py-14 text-center"
        >
          <div className="flex size-11 items-center justify-center rounded-full bg-destructive/10 text-destructive">
            <AlertCircle className="size-5" aria-hidden="true" />
          </div>
          <p className="text-sm font-medium text-foreground">Failed to load schemes.</p>
          <p className="text-sm text-muted-foreground">
            Please refresh the page or try again shortly.
          </p>
        </div>
      ) : filteredSchemes.length === 0 ? (
        <EmptySchemes hasActiveSearch={search.trim().length > 0} />
      ) : (
        <SchemeGrid schemes={filteredSchemes} />
      )}
    </div>
  );
}
