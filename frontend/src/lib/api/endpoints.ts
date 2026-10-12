export const API_ENDPOINTS = {
  LOGIN: "/api/v1/auth/login",
  REGISTER: "/api/v1/auth/register",

  GET_PROFILE: "/api/v1/citizen/profile",
  CREATE_PROFILE: "/api/v1/citizen/profile",
  UPDATE_PROFILE: "/api/v1/citizen/profile",
  GET_PROFILE_FACTS: "/api/v1/citizen/profile/facts",
  UPDATE_PROFILE_FACT: (factKey: string) =>
    `/api/v1/citizen/profile/facts/${factKey}`,

  GET_SCHEMES: "/api/v1/schemes",
  GET_ADMIN_SCHEMES: "/api/v1/schemes/admin",
  GET_RECOMMENDATIONS: "/api/v1/matching/recommendations",

  GET_APPLICATIONS: "/api/v1/applications",
  APPLY_TO_SCHEME: "/api/v1/applications",
  ADMIN_APPLICATIONS: "/api/v1/admin/applications",
  AI_EXPLAIN: "/api/v1/ai/explain",
  AI_CHAT: "/api/v1/ai/chat",  
} as const;
