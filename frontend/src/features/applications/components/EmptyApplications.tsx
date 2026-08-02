import { ClipboardList } from "lucide-react";
import Link from "next/link";

import { EmptyStateIllustration } from "@/components/illustrations/EmptyStateIllustration";
import { Button } from "@/components/ui/button";

export function EmptyApplications() {
  return (
    <div className="rounded-2xl border border-dashed border-border bg-muted/30 p-12 text-center">
      <EmptyStateIllustration icon={ClipboardList} />

      <h2 className="text-lg font-semibold text-foreground">
        No Applications Yet
      </h2>

      <p className="mx-auto mt-2 max-w-sm text-sm text-muted-foreground">
        Browse government schemes and submit your first application.
      </p>

      <Button className="mt-5" render={<Link href="/schemes" />}>
        Browse Schemes
      </Button>
    </div>
  );
}
