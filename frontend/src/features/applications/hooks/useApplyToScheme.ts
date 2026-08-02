"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { applyToScheme } from "../api/applicationsApi";

import { getErrorMessage } from "@/lib/api/errorHandler";

export function useApplyToScheme() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: applyToScheme,

    onSuccess: () => {
      toast.success("Application submitted successfully.");

      queryClient.invalidateQueries({
        queryKey: ["applications"],
      });

      queryClient.invalidateQueries({
        queryKey: ["recommendations"],
      });

      queryClient.invalidateQueries({
        queryKey: ["schemes"],
      });
    },

    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}