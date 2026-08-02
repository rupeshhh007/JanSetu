import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "../types";

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await axiosClient.post<LoginResponse>(
    API_ENDPOINTS.LOGIN,
    payload
  );

  return data;
}

export async function register(
  payload: RegisterRequest
): Promise<RegisterResponse> {
  const { data } = await axiosClient.post<RegisterResponse>(
    API_ENDPOINTS.REGISTER,
    payload
  );

  return data;
}