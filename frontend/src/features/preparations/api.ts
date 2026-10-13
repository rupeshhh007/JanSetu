import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import type { ApplicationPreparation } from "./types";

export async function getPreparations() {
  return (await axiosClient.get<ApplicationPreparation[]>(API_ENDPOINTS.PREPARATIONS)).data;
}
export async function startPreparation(schemeId: string) {
  return (await axiosClient.post<ApplicationPreparation>(`${API_ENDPOINTS.PREPARATIONS}/schemes/${schemeId}`)).data;
}
export async function updatePreparationTask(sessionId: string, taskId: string, complete: boolean) {
  return (await axiosClient.patch<ApplicationPreparation>(`${API_ENDPOINTS.PREPARATIONS}/${sessionId}/tasks/${taskId}`, { complete })).data;
}
export async function reportExternalApplication(sessionId: string) {
  return (await axiosClient.post<ApplicationPreparation>(`${API_ENDPOINTS.PREPARATIONS}/${sessionId}/report-applied`)).data;
}
