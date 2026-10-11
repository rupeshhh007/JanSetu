"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { login } from "../api/authApi";
import type { LoginRequest } from "../types";
import { useAuthStore } from "@/store/useAuthStore";
import { getErrorMessage } from "@/lib/api/errorHandler";

export function useLogin() {
  const router = useRouter();
  const setAuthData = useAuthStore((state) => state.setAuthData);

  return useMutation({
    mutationFn: (payload: LoginRequest) => login(payload),

    onSuccess: (data) => {
      setAuthData(data.accessToken, data.role);

      toast.success("Login successful!");

      router.push(data.role === "ADMIN" ? "/admin" : "/dashboard");
    },

    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}