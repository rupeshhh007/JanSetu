"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { updateProfileFact } from "../api/citizenApi";
import type { ProfileFactKey, ProfileFactRequest } from "../types";
import { getErrorMessage } from "@/lib/api/errorHandler";

export function useUpdateProfileFact() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ factKey, data }: { factKey: ProfileFactKey; data: ProfileFactRequest }) =>
      updateProfileFact(factKey, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["profile-facts"] });
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      toast.success("Fact sharing preference updated.");
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}
