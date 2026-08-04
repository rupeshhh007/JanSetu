import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type {
  ExplainRequest,
  ExplainResponse,
} from "../types";

export async function explainEligibility(
  request: ExplainRequest
): Promise<ExplainResponse> {
  const response =
    await axiosClient.post<ExplainResponse>(
      API_ENDPOINTS.AI_EXPLAIN,
      request
    );

  return response.data;
}