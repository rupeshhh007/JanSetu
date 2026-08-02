"use client";

import { useQuery } from "@tanstack/react-query";

import { getApplications } from "../api/applicationsApi";

export function useApplications() {
  return useQuery({
    queryKey: ["applications"],
    queryFn: getApplications,
  });
}