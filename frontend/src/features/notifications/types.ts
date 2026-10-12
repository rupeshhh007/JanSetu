export interface PolicyNotification {
  id: string;
  schemeId: string;
  schemeName: string;
  policyVersionId: string;
  policyVersionNumber: number;
  previousResult: "SATISFIED" | "FAILED" | "UNKNOWN" | "NOT_APPLICABLE" | null;
  currentResult: "SATISFIED" | "FAILED" | "UNKNOWN" | "NOT_APPLICABLE";
  title: string;
  message: string;
  readAt: string | null;
  createdAt: string;
}
