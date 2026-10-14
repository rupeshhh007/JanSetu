import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

/** The server derives citizen identity from the JWT, never a browser-supplied user ID. */
export async function getSavedSchemeIds(): Promise<string[]> {
  const { data } = await axiosClient.get<string[]>(API_ENDPOINTS.SAVED_SCHEMES);
  return data;
}

export async function saveScheme(id: string): Promise<void> {
  await axiosClient.put(`${API_ENDPOINTS.SAVED_SCHEMES}/${encodeURIComponent(id)}`);
}

export async function removeSavedScheme(id: string): Promise<void> {
  await axiosClient.delete(`${API_ENDPOINTS.SAVED_SCHEMES}/${encodeURIComponent(id)}`);
}
