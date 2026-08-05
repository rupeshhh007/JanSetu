export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  response: string;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
}

export interface ExplainRequest {
  userProfileSummary: string;
  schemeName: string;
  schemeDescription: string;
  matchedCriteria: string[];
}

export interface ExplainResponse {
  explanation: string;
}