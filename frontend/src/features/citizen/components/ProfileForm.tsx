"use client";

import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { User, MapPin, Wallet, ListChecks } from "lucide-react";

import { profileSchema, type ProfileFormValues } from "../schema/profileSchema";

import { useCreateProfile } from "../hooks/useCreateProfile";

import { FormField } from "@/components/forms/FormField";
import { EnumSelect } from "@/components/forms/EnumSelect";

import {
  GENDERS,
  CATEGORIES,
  MARITAL_STATUSES,
  OCCUPATIONS,
} from "@/lib/constants/profile";

import { Card, CardContent, CardHeader } from "@/components/ui/card";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import type { ProfileResponse, ProfileRequest } from "../types";
import { useEffect } from "react";
import { useUpdateProfile } from "../hooks/useUpdateProfile";

interface ProfileFormProps {
  initialData: ProfileResponse | null;
}

function SectionHeading({
  icon: Icon,
  title,
}: {
  icon: typeof User;
  title: string;
}) {
  return (
    <div className="flex items-center gap-2.5">
      <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
        <Icon className="size-4" strokeWidth={2} aria-hidden="true" />
      </div>
      <h2 className="text-base font-semibold text-foreground">{title}</h2>
    </div>
  );
}

export function ProfileForm({ initialData }: ProfileFormProps) {
  const createMutation = useCreateProfile();
  const updateMutation = useUpdateProfile();

  const isEdit = !!initialData;

  const isPending = createMutation.isPending || updateMutation.isPending;

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),

    defaultValues: {
      firstName: "",
      lastName: "",
      dateOfBirth: "",
      gender: "MALE",
      state: "",
      district: "",
      pincode: "",
      category: "GENERAL",
      annualFamilyIncome: 0,
      maritalStatus: "SINGLE",
      primaryOccupation: "UNEMPLOYED",
      isStudent: false,
      isFarmer: false,
      hasDisability: false,
    },
  });

  useEffect(() => {
    if (!initialData) return;

    reset({
      firstName: initialData.firstName,
      lastName: initialData.lastName,
      dateOfBirth: initialData.dateOfBirth,
      gender: initialData.gender,
      state: initialData.state,
      district: initialData.district,
      pincode: initialData.pincode,
      category: initialData.category,
      annualFamilyIncome: initialData.annualFamilyIncome,
      maritalStatus: initialData.maritalStatus,
      primaryOccupation: initialData.primaryOccupation,
      isStudent: initialData.isStudent,
      isFarmer: initialData.isFarmer,
      hasDisability: initialData.hasDisability,
    });
  }, [initialData, reset]);

  function onSubmit(values: ProfileRequest) {
    if (isEdit) {
      updateMutation.mutate(values);
    } else {
      createMutation.mutate(values);
    }
  }

  return (
    <Card className="mx-auto max-w-6xl">
      <CardHeader className="border-b-2 border-border pb-5">
        <p className="noticeboard-kicker text-primary">
          Your eligibility record
        </p>
        <h1 className="editorial mt-1 text-4xl">
          {isEdit ? "Your citizen profile" : "Build your profile"}
        </h1>
        <p className="mt-2 max-w-2xl text-sm text-text-muted">
          Use accurate information. JanSetu uses this only to explain possible
          scheme matches; confirm eligibility with the official department.
        </p>
      </CardHeader>

      <CardContent className="pt-6">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-10">
          {/* ---------------- PERSONAL INFORMATION ---------------- */}

          <section className="space-y-6">
            <SectionHeading icon={User} title="Personal Information" />

            <div className="grid gap-6 md:grid-cols-2">
              <FormField
                label="First Name"
                required
                error={errors.firstName?.message}
              >
                <Input
                  placeholder="Enter first name"
                  {...register("firstName")}
                />
              </FormField>

              <FormField
                label="Last Name"
                required
                error={errors.lastName?.message}
              >
                <Input
                  placeholder="Enter last name"
                  {...register("lastName")}
                />
              </FormField>

              <FormField
                label="Date of Birth"
                required
                error={errors.dateOfBirth?.message}
              >
                <Input type="date" {...register("dateOfBirth")} />
              </FormField>

              <FormField label="Gender" required error={errors.gender?.message}>
                <Controller
                  control={control}
                  name="gender"
                  render={({ field }) => (
                    <EnumSelect
                      value={field.value}
                      placeholder="Select gender"
                      options={GENDERS}
                      onChange={field.onChange}
                    />
                  )}
                />
              </FormField>
            </div>
          </section>

          {/* ---------------- ADDRESS ---------------- */}

          <section className="space-y-6">
            <SectionHeading icon={MapPin} title="Address" />

            <div className="grid gap-6 md:grid-cols-2">
              <FormField label="State" required error={errors.state?.message}>
                <Input placeholder="Tamil Nadu" {...register("state")} />
              </FormField>

              <FormField
                label="District"
                required
                error={errors.district?.message}
              >
                <Input placeholder="Chennai" {...register("district")} />
              </FormField>

              <FormField
                label="Pincode"
                required
                error={errors.pincode?.message}
              >
                <Input placeholder="600001" {...register("pincode")} />
              </FormField>
            </div>
          </section>

          {/* ---------------- SOCIO ECONOMIC ---------------- */}

          <section className="space-y-6">
            <SectionHeading icon={Wallet} title="Socio-Economic Information" />

            <div className="grid gap-6 md:grid-cols-2">
              <FormField
                label="Category"
                required
                error={errors.category?.message}
              >
                <Controller
                  control={control}
                  name="category"
                  render={({ field }) => (
                    <EnumSelect
                      value={field.value}
                      placeholder="Select category"
                      options={CATEGORIES}
                      onChange={field.onChange}
                    />
                  )}
                />
              </FormField>

              <FormField
                label="Annual Family Income"
                required
                error={errors.annualFamilyIncome?.message}
              >
                <Input
                  type="number"
                  placeholder="500000"
                  {...register("annualFamilyIncome", {
                    valueAsNumber: true,
                  })}
                />
              </FormField>
              <FormField
                label="Primary Occupation"
                required
                error={errors.primaryOccupation?.message}
              >
                <Controller
                  control={control}
                  name="primaryOccupation"
                  render={({ field }) => (
                    <EnumSelect
                      value={field.value}
                      placeholder="Select occupation"
                      options={OCCUPATIONS}
                      onChange={field.onChange}
                    />
                  )}
                />
              </FormField>

              <FormField
                label="Marital Status"
                required
                error={errors.maritalStatus?.message}
              >
                <Controller
                  control={control}
                  name="maritalStatus"
                  render={({ field }) => (
                    <EnumSelect
                      value={field.value}
                      placeholder="Select marital status"
                      options={MARITAL_STATUSES}
                      onChange={field.onChange}
                    />
                  )}
                />
              </FormField>
            </div>
          </section>

          {/* ---------------- ELIGIBILITY ---------------- */}

          <section className="space-y-6">
            <SectionHeading icon={ListChecks} title="Additional Information" />

            <div className="grid gap-4 md:grid-cols-3">
              <label className="flex min-h-14 items-center gap-3 border-2 border-border bg-surface p-4 cursor-pointer transition-colors hover:bg-accent/40">
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  {...register("isStudent")}
                />

                <span className="text-sm font-medium">Student</span>
              </label>

              <label className="flex min-h-14 items-center gap-3 border-2 border-border bg-surface p-4 cursor-pointer transition-colors hover:bg-accent/40">
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  {...register("isFarmer")}
                />

                <span className="text-sm font-medium">Farmer</span>
              </label>

              <label className="flex min-h-14 items-center gap-3 border-2 border-border bg-surface p-4 cursor-pointer transition-colors hover:bg-accent/40">
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  {...register("hasDisability")}
                />

                <span className="text-sm font-medium">
                  Person with Disability
                </span>
              </label>
            </div>
          </section>

          <div className="flex justify-end border-t border-border pt-6">
            <Button
              type="submit"
              disabled={isPending}
              className="h-12 min-w-44 rounded-none border border-border rounded-lg font-bold shadow-sm"
            >
              {isPending
                ? "Saving..."
                : isEdit
                  ? "Update Profile"
                  : "Create Profile"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
