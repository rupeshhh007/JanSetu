"use client";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import { useAuthStore } from "./useAuthStore";
// Per-account, on-device preferences. No citizen profile or document contents are stored here.
export function accountKey() {
  try {
    const token = useAuthStore.getState().accessToken;
    return token
      ? String(
          JSON.parse(
            atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
          ).sub,
        )
      : "anonymous";
  } catch {
    return "anonymous";
  }
}
interface WorkspaceState {
  saved: Record<string, string[]>;
  documents: Record<string, string[]>;
  toggleSaved: (id: string) => void;
  toggleDocument: (scheme: string, document: string) => void;
}
export const useWorkspaceStore = create<WorkspaceState>()(
  persist(
    (set) => ({
      saved: {},
      documents: {},
      toggleSaved: (id) =>
        set((state) => {
          const key = accountKey();
          const ids = state.saved[key] ?? [];
          return {
            saved: {
              ...state.saved,
              [key]: ids.includes(id)
                ? ids.filter((value) => value !== id)
                : [...ids, id],
            },
          };
        }),
      toggleDocument: (scheme, document) =>
        set((state) => {
          const key = accountKey() + ":" + scheme;
          const values = state.documents[key] ?? [];
          return {
            documents: {
              ...state.documents,
              [key]: values.includes(document)
                ? values.filter((value) => value !== document)
                : [...values, document],
            },
          };
        }),
    }),
    { name: "jansetu-workspace-v1" },
  ),
);
const EMPTY: string[] = [];
export function useSavedSchemes() {
  const key = accountKey();
  return useWorkspaceStore((state) => state.saved[key] ?? EMPTY);
}
export function useDocumentChecklist(id: string) {
  const key = accountKey() + ":" + id;
  return useWorkspaceStore((state) => state.documents[key] ?? EMPTY);
}
