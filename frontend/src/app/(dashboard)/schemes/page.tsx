"use client";

import { useMemo, useState } from "react";
import { FileText } from "lucide-react";

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

  if (isLoading) {
    return (
      <div className="space-y-6">
        <SchemeSkeleton />
      </div>
    );
  }

  if (isError) {
    return (
      <div className="text-center text-destructive">
        Failed to load schemes.
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center gap-3">
        <div className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <FileText className="size-5" strokeWidth={2} aria-hidden="true" />
        </div>

        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Government Schemes
          </h1>

          <p className="mt-1 text-muted-foreground">
            Browse all available government welfare schemes.
          </p>
        </div>
      </div>

      <SearchBar
        value={search}
        onChange={setSearch}
      />

      {filteredSchemes.length === 0 ? (
        <EmptySchemes />
      ) : (
        <SchemeGrid schemes={filteredSchemes} />
      )}
    </div>
  );
}
