import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type {
  ApplicationRequest,
  ApplicationResponse,
} from "../types";

export async function getApplications(): Promise<ApplicationResponse[]> {
  const response = await axiosClient.get<ApplicationResponse[]>(
    API_ENDPOINTS.GET_APPLICATIONS
  );

  return response.data;
}

export async function applyToScheme(
  request: ApplicationRequest
): Promise<ApplicationResponse> {
  const response = await axiosClient.post<ApplicationResponse>(
    API_ENDPOINTS.APPLY_TO_SCHEME,
    request
  );

  return response.data;
}