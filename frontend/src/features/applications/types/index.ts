export type ApplicationStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "UNDER_REVIEW"
  | "APPROVED"
  | "REJECTED"
  | "WITHDRAWN";

export interface ApplicationStatusHistory {
  id: string;
  previousStatus: ApplicationStatus | null;
  newStatus: ApplicationStatus;
  remarks: string | null;
  changedBy: string;
  changedAt: string;
}

export interface ApplicationRequest {
  schemeId: string;
}

export interface ApplicationResponse {
  applicationId: string;
  schemeId: string;
  schemeName: string;
  status: ApplicationStatus;
  remarks: string | null;
  submittedAt: string;
  updatedAt: string;
  history: ApplicationStatusHistory[];
}
