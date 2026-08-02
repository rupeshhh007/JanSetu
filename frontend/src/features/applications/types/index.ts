export type ApplicationStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "UNDER_REVIEW"
  | "APPROVED"
  | "REJECTED";

export interface ApplicationRequest {
  schemeId: string;
}

export interface ApplicationResponse {
  applicationId: string;
  schemeId: string;
  schemeName: string;
  status: ApplicationStatus;
  remarks: string;
  submittedAt: string;
  updatedAt: string;
}