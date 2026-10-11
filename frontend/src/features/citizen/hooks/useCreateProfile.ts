"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { createProfile } from "../api/citizenApi";
import { getErrorMessage } from "@/lib/api/errorHandler";

export function useCreateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createProfile,

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["eligibility"] });
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      toast.success("Profile created successfully");

      queryClient.invalidateQueries({
        queryKey: ["profile"],
      });
    },

    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}