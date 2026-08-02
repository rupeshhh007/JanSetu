import type { RecommendationResponse } from "../types";
import { RecommendationCard } from "./RecommendationCard";

interface RecommendationGridProps {
  recommendations: RecommendationResponse[];
}

export function RecommendationGrid({
  recommendations,
}: RecommendationGridProps) {
  return (
    <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      {recommendations.map((recommendation) => (
        <RecommendationCard
          key={recommendation.scheme.id}
          recommendation={recommendation}
        />
      ))}
    </div>
  );
}