"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getErrorMessage } from "@/lib/api/errorHandler";
import { getPreparations, reportExternalApplication, startPreparation, updatePreparationTask } from "./api";

export function usePreparations() {
  return useQuery({ queryKey: ["preparations"], queryFn: getPreparations });
}

export function useStartPreparation() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: startPreparation,
    onSuccess: async () => { await client.invalidateQueries({ queryKey: ["preparations"] }); toast.success("Preparation journey saved."); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });
}

export function useUpdatePreparationTask() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ sessionId, taskId, complete }: { sessionId: string; taskId: string; complete: boolean }) =>
      updatePreparationTask(sessionId, taskId, complete),
    onSuccess: async () => { await client.invalidateQueries({ queryKey: ["preparations"] }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });
}

export function useReportExternalApplication() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: reportExternalApplication,
    onSuccess: async () => { await client.invalidateQueries({ queryKey: ["preparations"] }); toast.success("Your external application report was recorded."); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });
}
