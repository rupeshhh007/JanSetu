import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type { RecommendationResponse } from "../types";

export async function getRecommendations(): Promise<
  RecommendationResponse[]
> {
  const response = await axiosClient.get<RecommendationResponse[]>(
    API_ENDPOINTS.GET_RECOMMENDATIONS
  );

  return response.data;
}