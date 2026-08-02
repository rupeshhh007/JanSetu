import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type {
  ProfileRequest,
  ProfileResponse,
} from "../types";

import axios from "axios";

export async function getProfile() {
  try {
    const response = await axiosClient.get<ProfileResponse>(
      API_ENDPOINTS.GET_PROFILE
    );

    return response.data;
  } catch (error) {
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 404
    ) {
      return null;
    }

    throw error;
  }
}

export async function createProfile(
  data: ProfileRequest
) {
  const response = await axiosClient.post<ProfileResponse>(
    API_ENDPOINTS.CREATE_PROFILE,
    data
  );

  return response.data;
}

export async function updateProfile(
  data: ProfileRequest
) {
  const response = await axiosClient.put<ProfileResponse>(
    API_ENDPOINTS.UPDATE_PROFILE,
    data
  );

  return response.data;
}