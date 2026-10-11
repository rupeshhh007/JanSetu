"use client";

import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Mail, Lock } from "lucide-react";

import { loginSchema, type LoginFormValues } from "../schema/authSchemas";
import { useLogin } from "../hooks/useLogin";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function LoginForm() {
  const { mutate, isPending } = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  function onSubmit(values: LoginFormValues) {
    mutate(values);
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <div>
        <label htmlFor="login-email" className="mb-2 block text-sm font-bold">
          Email
        </label>

        <div className="relative">
          <Mail
            className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />

          <Input
            id="login-email"
            type="email"
            placeholder="Enter your email"
            className="h-10 pl-9"
            {...register("email")}
          />
        </div>

        {errors.email && (
          <p
            id="login-email-error"
            className="mt-1 text-sm font-medium text-destructive"
            role="alert"
          >
            {errors.email.message}
          </p>
        )}
      </div>

      <div>
        <label
          htmlFor="login-password"
          className="mb-2 block text-sm font-bold"
        >
          Password
        </label>

        <div className="relative">
          <Lock
            className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />

          <Input
            id="login-password"
            type="password"
            placeholder="Enter your password"
            className="h-10 pl-9"
            {...register("password")}
          />
        </div>

        {errors.password && (
          <p
            id="login-password-error"
            className="mt-1 text-sm font-medium text-destructive"
            role="alert"
          >
            {errors.password.message}
          </p>
        )}
      </div>

      <Button
        type="submit"
        disabled={isPending}
        className="h-12 w-full rounded-none border border-border rounded-lg font-bold shadow-sm"
      >
        {isPending ? "Signing In..." : "Sign In"}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Do not have an account?{" "}
        <Link
          href="/register"
          className="font-medium text-primary hover:underline"
        >
          Register
        </Link>
      </p>
    </form>
  );
}
