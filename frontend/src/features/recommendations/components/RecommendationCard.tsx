import Link from "next/link";
import { ArrowRight, CheckCircle2, ShieldCheck } from "lucide-react";
import type { RecommendationResponse } from "../types";
export function RecommendationCard({
  recommendation,
}: {
  recommendation: RecommendationResponse;
}) {
  const { scheme, matchedCriteria } = recommendation;
  return (
    <article className="flex h-full flex-col rounded-xl border bg-white p-6">
      <div className="mb-4 flex items-center gap-2 text-xs font-medium text-primary">
        <ShieldCheck size={16} />
        Recorded requirements met
      </div>
      <h2 className="text-lg leading-snug">{scheme.name}</h2>
      <p className="mt-3 text-sm leading-6 text-muted-foreground">
        {scheme.description}
      </p>
      <h3 className="mb-3 mt-5 text-sm">Why this appears</h3>
      <ul className="mb-6 flex-1 space-y-3">
        {matchedCriteria.map((criterion) => (
          <li key={criterion} className="flex gap-2 text-xs leading-5">
            <CheckCircle2 size={15} className="mt-0.5 shrink-0 text-primary" />
            {criterion}
          </li>
        ))}
      </ul>
      <Link href={`/schemes/${scheme.id}`} className="text-link border-t pt-4">
        Review details & next steps <ArrowRight size={16} />
      </Link>
    </article>
  );
}
