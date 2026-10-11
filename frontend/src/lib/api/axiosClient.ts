import axios from "axios";
import { useAuthStore } from "@/store/useAuthStore";

export const axiosClient = axios.create({
  baseURL:
    process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080",
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});
axiosClient.interceptors.response.use(response => response, error => {
  const status = error.response?.status;
  const path = error.config?.url ?? "";
  if (status === 401 && !path.includes("/auth/")) useAuthStore.getState().logout();
  return Promise.reject(error);
});
