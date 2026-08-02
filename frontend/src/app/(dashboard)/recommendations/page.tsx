"use client";

import { Sparkles } from "lucide-react";

import { EmptyRecommendations } from "@/features/recommendations/components/EmptyRecommendations";
import { RecommendationGrid } from "@/features/recommendations/components/RecommendationGrid";
import { RecommendationSkeleton } from "@/features/recommendations/components/RecommendationSkeleton";
import { useRecommendations } from "@/features/recommendations/hooks/useRecommendations";

function PageHeader() {
  return (
    <div className="flex items-center gap-3">
      <div className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-info/10 text-info">
        <Sparkles className="size-5" strokeWidth={2} aria-hidden="true" />
      </div>

      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Personalized Recommendations
        </h1>
      </div>
    </div>
  );
}

export default function RecommendationsPage() {
  const {
    data: recommendations,
    isLoading,
    isError,
  } = useRecommendations();

  if (isLoading) {
    return (
      <div className="space-y-6">
        <PageHeader />

        <RecommendationSkeleton />
      </div>
    );
  }

  if (isError) {
    return (
      <p className="text-destructive">
        Failed to load recommendations.
      </p>
    );
  }

  if (!recommendations?.length) {
    return (
      <div className="space-y-6">
        <PageHeader />

        <EmptyRecommendations />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <div className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-info/10 text-info">
          <Sparkles className="size-5" strokeWidth={2} aria-hidden="true" />
        </div>

        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Personalized Recommendations
          </h1>

          <p className="mt-1 text-muted-foreground">
            Schemes tailored to your profile and eligibility.
          </p>
        </div>
      </div>

      <RecommendationGrid recommendations={recommendations} />
    </div>
  );
}
