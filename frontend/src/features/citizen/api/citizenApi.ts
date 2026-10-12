import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";

import type {
  ProfileRequest,
  ProfileResponse,
  ProfileFactKey,
  ProfileFactRequest,
  ProfileFactResponse,
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

export async function getProfileFacts() {
  const response = await axiosClient.get<ProfileFactResponse[]>(
    API_ENDPOINTS.GET_PROFILE_FACTS,
  );
  return response.data;
}

export async function updateProfileFact(
  factKey: ProfileFactKey,
  data: ProfileFactRequest,
) {
  const response = await axiosClient.put<ProfileFactResponse>(
    API_ENDPOINTS.UPDATE_PROFILE_FACT(factKey),
    data,
  );
  return response.data;
}
