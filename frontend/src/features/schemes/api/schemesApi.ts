import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type { SchemeCreateRequest, SchemeResponse } from "../types";

export async function getSchemes(): Promise<SchemeResponse[]> {
  const response = await axiosClient.get<SchemeResponse[]>(
    API_ENDPOINTS.GET_SCHEMES
  );
  return response.data;
}

export async function getAdminSchemes(): Promise<SchemeResponse[]> {
  const response = await axiosClient.get<SchemeResponse[]>(
    API_ENDPOINTS.GET_ADMIN_SCHEMES
  );
  return response.data;
}

export async function createScheme(data: SchemeCreateRequest): Promise<SchemeResponse> {
  const response = await axiosClient.post<SchemeResponse>(
    API_ENDPOINTS.GET_SCHEMES,
    data
  );
  return response.data;
}

export async function deleteScheme(id: string): Promise<void> {
  await axiosClient.delete(`${API_ENDPOINTS.GET_SCHEMES}/${id}`);
}

export async function publishScheme(id: string): Promise<SchemeResponse> {
  const response = await axiosClient.post<SchemeResponse>(`${API_ENDPOINTS.GET_SCHEMES}/${id}/publish`);
  return response.data;
}

export async function getSchemeEligibility(id: string): Promise<import("../types").EligibilityEvaluation> {
  return (await axiosClient.get(`${API_ENDPOINTS.GET_SCHEMES}/${id}/eligibility`)).data;
}
