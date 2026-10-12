import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import type { PolicyNotification } from "./types";

export async function getPolicyNotifications() {
  return (await axiosClient.get<PolicyNotification[]>(API_ENDPOINTS.NOTIFICATIONS)).data;
}

export async function markPolicyNotificationRead(notificationId: string) {
  return (await axiosClient.patch<PolicyNotification>(`${API_ENDPOINTS.NOTIFICATIONS}/${notificationId}/read`)).data;
}
