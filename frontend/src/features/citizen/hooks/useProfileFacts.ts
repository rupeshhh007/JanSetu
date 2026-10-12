"use client";

import { useQuery } from "@tanstack/react-query";

import { getProfileFacts } from "../api/citizenApi";

export function useProfileFacts(enabled: boolean) {
  return useQuery({
    queryKey: ["profile-facts"],
    queryFn: getProfileFacts,
    enabled,
  });
}
