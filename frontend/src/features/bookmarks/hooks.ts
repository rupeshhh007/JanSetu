"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useAuthStore } from "@/store/useAuthStore";
import { getErrorMessage } from "@/lib/api/errorHandler";
import { getSavedSchemeIds, removeSavedScheme, saveScheme } from "./api";

const SAVED_KEY = ["saved-schemes"] as const;
const EMPTY: string[] = [];

export function useSavedSchemeIds() {
  const authenticated = useAuthStore((s) => s.isAuthenticated);
  return useQuery({
    queryKey: SAVED_KEY,
    queryFn: getSavedSchemeIds,
    enabled: authenticated,
    staleTime: 60_000,
  });
}

/** Compatibility helper for catalogue components; useSavedSchemeIds surfaces errors. */
export function useSavedSchemes(): string[] {
  return useSavedSchemeIds().data ?? EMPTY;
}

export function useToggleSavedScheme() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ schemeId, saved }: { schemeId: string; saved: boolean }) =>
      saved ? removeSavedScheme(schemeId) : saveScheme(schemeId),
    onMutate: async ({ schemeId, saved }) => {
      await client.cancelQueries({ queryKey: SAVED_KEY });
      const previous = client.getQueryData<string[]>(SAVED_KEY);
      client.setQueryData<string[]>(SAVED_KEY, (current) =>
        saved
          ? (current ?? []).filter((id) => id !== schemeId)
          : [...new Set([...(current ?? []), schemeId])],
      );
      return { previous };
    },
    onError: (error, _input, context) => {
      if (context?.previous !== undefined) client.setQueryData(SAVED_KEY, context.previous);
      else client.removeQueries({ queryKey: SAVED_KEY });
      toast.error(getErrorMessage(error));
    },
    onSettled: () => client.invalidateQueries({ queryKey: SAVED_KEY }),
  });
}
