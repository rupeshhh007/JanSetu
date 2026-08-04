import { FileSearch } from "lucide-react";

import { EmptyStateIllustration } from "@/components/illustrations/EmptyStateIllustration";

interface EmptySchemesProps {
  /** True when the empty result is caused by the active search query, not a genuinely empty dataset. */
  hasActiveSearch?: boolean;
}

export function EmptySchemes({ hasActiveSearch = false }: EmptySchemesProps) {
  return (
    <div className="rounded-2xl border border-dashed border-border bg-muted/30 px-6 py-16 text-center">
      <EmptyStateIllustration icon={FileSearch} />

      <h2 className="text-lg font-semibold text-foreground">
        {hasActiveSearch ? "No matching schemes" : "No Schemes Found"}
      </h2>

      <p className="mx-auto mt-2 max-w-sm text-sm text-muted-foreground">
        {hasActiveSearch
          ? "Try a different keyword or clear your search to see all available schemes."
          : "There are currently no government schemes available."}
      </p>
    </div>
  );
}
