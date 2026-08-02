import { FileSearch } from "lucide-react";

import { EmptyStateIllustration } from "@/components/illustrations/EmptyStateIllustration";

export function EmptySchemes() {
  return (
    <div className="rounded-2xl border border-dashed border-border bg-muted/30 p-12 text-center">
      <EmptyStateIllustration icon={FileSearch} />

      <h2 className="text-lg font-semibold text-foreground">
        No Schemes Found
      </h2>

      <p className="mx-auto mt-2 max-w-sm text-sm text-muted-foreground">
        There are currently no government schemes available.
      </p>
    </div>
  );
}
