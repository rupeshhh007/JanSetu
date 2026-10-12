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

export type ProfileFactKey =
  | "BIRTH_DATE"
  | "ANNUAL_FAMILY_INCOME"
  | "CATEGORY"
  | "PRIMARY_OCCUPATION"
  | "STATE"
  | "IS_STUDENT"
  | "IS_FARMER"
  | "HAS_DISABILITY";

export type ProfileFactProvenance =
  | "CITIZEN_DECLARED"
  | "DOCUMENT_VERIFIED"
  | "OFFICIAL_RECORD";

export type ProfileFactVerificationStatus =
  | "SELF_DECLARED"
  | "DOCUMENT_VERIFIED"
  | "AUTHORITATIVE";

export type ProfileFactConsentScope = "PRIVATE" | "CASEWORKER_REUSE";

export interface ProfileFactResponse {
  factKey: ProfileFactKey;
  value: string;
  provenance: ProfileFactProvenance;
  verificationStatus: ProfileFactVerificationStatus;
  consentScope: ProfileFactConsentScope;
  expiresAt: string | null;
  capturedAt: string;
  active: boolean;
}

export interface ProfileFactRequest {
  value: string;
  consentToCaseworkerReuse: boolean;
  expiresAt: string | null;
}
