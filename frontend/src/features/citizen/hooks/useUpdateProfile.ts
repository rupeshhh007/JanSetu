"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { updateProfile } from "../api/citizenApi";
import { getErrorMessage } from "@/lib/api/errorHandler";

export function useUpdateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateProfile,

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["eligibility"] });
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      toast.success("Profile updated successfully");

      queryClient.invalidateQueries({
        queryKey: ["profile"],
      });
    },

    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}