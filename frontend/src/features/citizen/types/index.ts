export type Gender = "MALE" | "FEMALE" | "OTHER";

export type Category =
  | "GENERAL"
  | "OBC"
  | "SC"
  | "ST";

export type MaritalStatus =
  | "SINGLE"
  | "MARRIED"
  | "WIDOWED"
  | "DIVORCED";

export type PrimaryOccupation =
  | "SALARIED"
  | "BUSINESS"
  | "DAILY_WAGE"
  | "UNEMPLOYED"
  | "STUDENT"
  | "FARMER";

export interface ProfileRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;

  gender: Gender;

  state: string;
  district: string;
  pincode: string;

  category: Category;

  annualFamilyIncome: number;

  maritalStatus: MaritalStatus;

  primaryOccupation: PrimaryOccupation;

  isStudent: boolean;
  isFarmer: boolean;
  hasDisability: boolean;
}

export interface ProfileResponse {
  profileId: string;

  firstName: string;
  lastName: string;
  dateOfBirth: string;

  gender: Gender;

  state: string;
  district: string;
  pincode: string;

  category: Category;

  annualFamilyIncome: number;

  maritalStatus: MaritalStatus;

  primaryOccupation: PrimaryOccupation;

  isStudent: boolean;
  isFarmer: boolean;
  hasDisability: boolean;

  lastUpdated: string;
}