import type {
  Category,
  Gender,
  MaritalStatus,
  PrimaryOccupation,
} from "@/features/citizen/types";

export const GENDERS: Gender[] = [
  "MALE",
  "FEMALE",
  "OTHER",
];

export const CATEGORIES: Category[] = [
  "GENERAL",
  "OBC",
  "SC",
  "ST",
];

export const MARITAL_STATUSES: MaritalStatus[] = [
  "SINGLE",
  "MARRIED",
  "WIDOWED",
  "DIVORCED",
];

export const OCCUPATIONS: PrimaryOccupation[] = [
  "SALARIED",
  "BUSINESS",
  "DAILY_WAGE",
  "UNEMPLOYED",
  "STUDENT",
  "FARMER",
];