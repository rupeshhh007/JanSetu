import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type { SchemeResponse } from "../types";

export async function getSchemes(): Promise<SchemeResponse[]> {
  const response = await axiosClient.get<SchemeResponse[]>(
    API_ENDPOINTS.GET_SCHEMES
  );

  return response.data;
}