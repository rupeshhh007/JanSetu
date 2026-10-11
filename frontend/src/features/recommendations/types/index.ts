import type { SchemeResponse } from "@/features/schemes/types";

export interface RecommendationResponse {
  scheme: SchemeResponse;
  matchPercentage: number;
  matchedCriteria: string[];
  eligibilityStatus: "ELIGIBLE" | "MAYBE_ELIGIBLE" | "NOT_ELIGIBLE" | "NEED_MORE_INFO";
  missingInformation: string[];
}
