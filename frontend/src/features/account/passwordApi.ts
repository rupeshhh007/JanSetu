import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

/** No password values may be logged, cached or returned to React Query state. */
export async function changeAccountPassword(currentPassword: string, newPassword: string): Promise<void> {
  await axiosClient.post(API_ENDPOINTS.CHANGE_PASSWORD, { currentPassword, newPassword });
}
