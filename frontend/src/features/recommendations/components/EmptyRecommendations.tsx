import Link from "next/link";
import { Sparkles } from "lucide-react";

export function EmptyRecommendations() {
  return (
    <div className="border-2 border-dashed border-foreground bg-surface p-10 text-center shadow-sm">
      <Sparkles className="mx-auto size-9 text-primary" aria-hidden="true" />
      <h2 className="mt-4 text-2xl font-bold">Build your match list</h2>

      <p className="mx-auto mt-2 max-w-md text-text-muted">
        Complete your profile so JanSetu can explain which scheme criteria you
        may meet.
      </p>
      <Link
        href="/profile"
        className="mt-5 inline-flex min-h-11 items-center border border-border rounded-lg bg-accent px-4 font-bold shadow-sm"
      >
        Complete profile
      </Link>
    </div>
  );
}
