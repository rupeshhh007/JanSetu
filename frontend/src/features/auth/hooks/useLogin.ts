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
  const setAccessToken = useAuthStore((state) => state.setAccessToken);

  return useMutation({
    mutationFn: (payload: LoginRequest) => login(payload),

    onSuccess: (data) => {
      setAccessToken(data.accessToken);

      toast.success("Login successful!");

      router.push("/dashboard");
    },

    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}