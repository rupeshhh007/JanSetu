"use client";

import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Mail, Lock, ShieldCheck } from "lucide-react";

import { registerSchema, type RegisterFormValues } from "../schema/authSchemas";
import { useRegister } from "../hooks/useRegister";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export function RegisterForm() {
  const { mutate, isPending } = useRegister();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  function onSubmit(values: RegisterFormValues) {
    mutate({
      email: values.email,
      password: values.password,
    });
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      <div>
        <label
          htmlFor="register-email"
          className="mb-2 block text-sm font-bold"
        >
          Email
        </label>

        <div className="relative">
          <Mail
            className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />

          <Input
            id="register-email"
            type="email"
            placeholder="Enter your email"
            className="h-10 pl-9"
            {...register("email")}
          />
        </div>

        {errors.email && (
          <p className="mt-1 text-sm text-destructive">
            {errors.email.message}
          </p>
        )}
      </div>

      <div>
        <label
          htmlFor="register-password"
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
            id="register-password"
            type="password"
            placeholder="Create a password"
            className="h-10 pl-9"
            {...register("password")}
          />
        </div>

        {errors.password && (
          <p className="mt-1 text-sm text-destructive">
            {errors.password.message}
          </p>
        )}
      </div>

      <div>
        <label
          htmlFor="register-confirm-password"
          className="mb-2 block text-sm font-bold"
        >
          Confirm Password
        </label>

        <div className="relative">
          <ShieldCheck
            className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />

          <Input
            id="register-confirm-password"
            type="password"
            placeholder="Confirm password"
            className="h-10 pl-9"
            {...register("confirmPassword")}
          />
        </div>

        {errors.confirmPassword && (
          <p className="mt-1 text-sm text-destructive">
            {errors.confirmPassword.message}
          </p>
        )}
      </div>

      <Button
        type="submit"
        disabled={isPending}
        className="h-12 w-full rounded-none border border-border rounded-lg font-bold shadow-sm"
      >
        {isPending ? "Creating Account..." : "Create Account"}
      </Button>

      <p className="text-center text-sm text-muted-foreground">
        Already have an account?{" "}
        <Link
          href="/login"
          className="font-medium text-primary hover:underline"
        >
          Login
        </Link>
      </p>
    </form>
  );
}
