export interface ExplainRequest {
  userProfileSummary: string;
  schemeName: string;
  schemeDescription: string;
  matchedCriteria: string[];
}

export interface ExplainResponse {
  explanation: string;
}