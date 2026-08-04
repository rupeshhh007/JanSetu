"use client";

import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

import { explainEligibility } from "../api/aiApi";
import { getErrorMessage } from "@/lib/api/errorHandler";

export function useExplainEligibility() {
  return useMutation({
    mutationFn: explainEligibility,

    onError(error) {
      toast.error(getErrorMessage(error));
    },
  });
}