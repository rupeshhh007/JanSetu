"use client";

import Link from "next/link";
import { ArrowRight, ShieldCheck, Sparkles } from "lucide-react";

import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

import type { RecommendationResponse } from "@/features/recommendations/types";

interface TopRecommendationPreviewProps {
  /** Comes straight from the existing useRecommendations() query result. */
  recommendations: RecommendationResponse[];
}

export function TopRecommendationPreview({
  recommendations,
}: TopRecommendationPreviewProps) {
  const top = [...recommendations].sort(
    (a, b) => b.matchPercentage - a.matchPercentage
  )[0];

  return (
    <Card
      className={cn(
        "relative overflow-hidden border border-border transition-all duration-300 ease-out",
        "hover:-translate-y-1 hover:border-foreground/15 hover:shadow-[var(--shadow-card-hover)]"
      )}
    >
      <div
        className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/[0.05] via-transparent to-success/[0.06]"
        aria-hidden="true"
      />

      <CardContent className="relative p-6">
        <div className="flex items-center gap-2">
          <span className="flex size-7 items-center justify-center rounded-lg bg-gradient-to-br from-primary/15 to-success/15 text-primary">
            <Sparkles className="size-3.5" aria-hidden="true" />
          </span>
          <p className="text-xs font-semibold tracking-wide text-muted-foreground uppercase">
            AI-matched for you
          </p>
        </div>

        {!top ? (
          <div className="flex flex-col items-center gap-3 py-8 text-center">
            <p className="text-sm text-muted-foreground">
              Complete your profile to get personalized scheme matches.
            </p>
          </div>
        ) : (
          <div className="mt-4 space-y-4">
            <div className="flex items-start justify-between gap-3">
              <p className="text-base font-semibold leading-snug text-foreground">
                {top.scheme.name}
              </p>

              <div className="flex shrink-0 flex-col items-end">
                <span className="text-2xl font-bold tabular-nums text-success">
                  {top.matchPercentage}%
                </span>
                <span className="text-[0.65rem] font-medium tracking-wide text-muted-foreground uppercase">
                  match
                </span>
              </div>
            </div>

            <p className="line-clamp-2 text-sm text-muted-foreground">
              {top.scheme.description}
            </p>

            {top.matchedCriteria.length > 0 && (
              <ul className="space-y-1.5 border-t border-border pt-3">
                {top.matchedCriteria.slice(0, 2).map((criterion) => (
                  <li
                    key={criterion}
                    className="flex items-start gap-2 text-xs text-muted-foreground"
                  >
                    <ShieldCheck
                      className="mt-0.5 size-3.5 shrink-0 text-success"
                      aria-hidden="true"
                    />
                    <span>{criterion}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        <Link
          href="/recommendations"
          className="mt-5 flex items-center gap-1 rounded-md text-sm font-medium text-primary hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        >
          View all recommendations
          <ArrowRight className="size-3.5" aria-hidden="true" />
        </Link>
      </CardContent>
    </Card>
  );
}
