import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type {
  ChatRequest,
  ChatResponse,
  ExplainRequest,
  ExplainResponse,
} from "../types";

export async function explainEligibility(
  request: ExplainRequest
): Promise<ExplainResponse> {
  const { data } = await axiosClient.post<ExplainResponse>(
    API_ENDPOINTS.AI_EXPLAIN,
    request
  );

  return data;
}

export async function chatWithAI(
  request: ChatRequest
): Promise<ChatResponse> {
  const { data } = await axiosClient.post<ChatResponse>(
    API_ENDPOINTS.AI_CHAT,
    request
  );

  return data;
}