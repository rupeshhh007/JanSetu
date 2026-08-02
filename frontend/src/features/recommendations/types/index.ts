import type { SchemeResponse } from "@/features/schemes/types";

export interface RecommendationResponse {
  scheme: SchemeResponse;
  matchPercentage: number;
  matchedCriteria: string[];
}