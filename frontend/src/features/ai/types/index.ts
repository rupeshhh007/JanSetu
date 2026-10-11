export interface ChatRequest {
  message: string;
  contextSchemeIds?: string[];
}

export interface ChatResponse {
  response: string;
  citations: Citation[];
  cards: ChatCard[];
  followUpQuestions: string[];
  fallback: boolean;
}

export interface Citation {
  title: string;
  sourceName: string | null;
  url: string | null;
  lastVerifiedAt: string | null;
}

export interface ChatCard {
  type: string;
  title: string;
  description: string;
  schemeId: string | null;
  highlights: string[];
  actionLabel: string | null;
  actionHref: string | null;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  citations?: Citation[];
  cards?: ChatCard[];
  followUpQuestions?: string[];
  fallback?: boolean;
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
