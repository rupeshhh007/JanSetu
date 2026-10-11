export type Category =
  | "GENERAL"
  | "OBC"
  | "SC"
  | "ST";

export type PrimaryOccupation =
  | "SALARIED"
  | "BUSINESS"
  | "DAILY_WAGE"
  | "UNEMPLOYED"
  | "STUDENT"
  | "FARMER";

export interface EligibilityCriteria {
  minAge: number | null;
  maxAge: number | null;
  maxFamilyIncome: number | null;

  requiredCategories: Category[] | null;
  allowedOccupations: PrimaryOccupation[] | null;

  requiresStudent: boolean | null;
  requiresFarmer: boolean | null;
  requiresDisability: boolean | null;
}

export interface SchemeResponse {
  id: string;
  name: string;
  description: string;
  state: string;
  department: string;
  officialUrl: string | null;
  sourceName: string | null;
  lastVerifiedAt: string | null;
  benefits: string[];
  requiredDocuments: string[];
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  publishedAt: string | null;
  archivedAt: string | null;
  updatedAt: string;
  eligibilityCriteria: EligibilityCriteria;
}

/** Current admin contract. Kept separate so API enrichments can be adopted without weakening types. */
export interface SchemeCreateRequest {
  name: string;
  description: string;
  state: string;
  department: string;
  officialUrl?: string;
  sourceName?: string;
  lastVerifiedAt?: string;
  benefits?: string[];
  requiredDocuments?: string[];
  eligibilityCriteria: Partial<EligibilityCriteria>;
}

export interface EligibilityEvaluation {
  outcome: "ELIGIBLE" | "NOT_ELIGIBLE" | "NEEDS_INFORMATION";
  matchedCriteria: string[];
  unmetCriteria: string[];
  missingInformation: string[];
  evaluatedRuleCount: number;
}
