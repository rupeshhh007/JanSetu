import type { SchemeResponse } from "@/features/schemes/types";

export interface RecommendationResponse {
  scheme: SchemeResponse;
  /** Compatibility alias for evidenceScore. It is not approval probability. */
  matchPercentage: number;
  evidenceScore: number;
  recordedRuleCount: number;
  recommendationState:
    | "READY_TO_REVIEW"
    | "PROFILE_INFORMATION_NEEDED"
    | "POLICY_INFORMATION_UNAVAILABLE";
  requiresProfileAction: boolean;
  nextStep: string;
  matchedCriteria: string[];
  unmetCriteria: string[];
  eligibilityStatus: "ELIGIBLE" | "NOT_ELIGIBLE" | "NEEDS_INFORMATION";
  missingInformation: string[];
}
