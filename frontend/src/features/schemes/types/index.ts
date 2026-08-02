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
  eligibilityCriteria: EligibilityCriteria;
}