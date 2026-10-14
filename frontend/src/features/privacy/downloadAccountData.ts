import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

/** Do not log or cache the sensitive export; download it only after a user action. */
export async function downloadAccountData(): Promise<void> {
  const response = await axiosClient.get<Blob>(API_ENDPOINTS.DATA_EXPORT, {
    responseType: "blob",
    headers: { Accept: "application/json" },
  });
  const blob = new Blob([response.data], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  try {
    const link = document.createElement("a");
    link.href = url;
    link.download = "jansetu-my-data.json";
    link.style.display = "none";
    document.body.appendChild(link);
    link.click();
    link.remove();
  } finally {
    // Deferring revocation protects download behavior in slower browsers.
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
