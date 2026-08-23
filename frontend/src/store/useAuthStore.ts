import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

interface AuthState {
  accessToken: string | null;
  role: string | null;
  isAuthenticated: boolean;

  setAuthData: (token: string, role: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      role: null,
      isAuthenticated: false,

      setAuthData: (token, role) =>
        set({
          accessToken: token,
          role: role,
          isAuthenticated: true,
        }),

      logout: () =>
        set({
          accessToken: null,
          role: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: "jansetu-auth",
      storage: createJSONStorage(() => localStorage),
    }
  )
);