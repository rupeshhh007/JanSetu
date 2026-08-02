"use client";

import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { register } from "../api/authApi";
import type { RegisterRequest } from "../types";
import { getErrorMessage } from "@/lib/api/errorHandler";

export function useRegister() {
  const router = useRouter();

  return useMutation({
    mutationFn: (payload: RegisterRequest) => register(payload),

    onSuccess: (data) => {
      toast.success(data.message);

      router.push("/login");
    },

    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });
}