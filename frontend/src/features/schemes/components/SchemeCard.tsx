"use client";

import {
  Landmark,
  Calendar,
  Wallet,
  Users,
  Briefcase,
  GraduationCap,
  Sprout,
  Accessibility,
} from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import { useApplyToScheme } from "@/features/applications/hooks/useApplyToScheme";
import { useApplications } from "@/features/applications/hooks/useApplications";

import type { SchemeResponse } from "../types";

interface SchemeCardProps {
  scheme: SchemeResponse;
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

function EligibilityRow({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof Calendar;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-start gap-2.5">
      <Icon className="mt-0.5 size-4 shrink-0 text-muted-foreground" strokeWidth={2} aria-hidden="true" />
      <p className="text-sm">
        <span className="font-medium text-foreground">{label}: </span>
        <span className="text-muted-foreground">{value}</span>
      </p>
    </div>
  );
}

export function SchemeCard({ scheme }: SchemeCardProps) {
  const eligibility = scheme.eligibilityCriteria ?? {};

  const { mutate, isPending } = useApplyToScheme();

  const { data: applications = [] } = useApplications();

  const alreadyApplied = applications.some(
    (application) => application.schemeId === scheme.id
  );

  function handleApply() {
    mutate({
      schemeId: scheme.id,
    });
  }

  return (
    <Card
      className="group flex h-full flex-col border border-border transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-[var(--shadow-card-hover)]"
    >
      <CardHeader className="flex items-start gap-3">
        <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary transition-transform duration-200 group-hover:scale-105">
          <Landmark className="size-5" strokeWidth={2} aria-hidden="true" />
        </div>
        <CardTitle className="pt-1 leading-snug">{scheme.name}</CardTitle>
      </CardHeader>

      <CardContent className="flex flex-1 flex-col space-y-4 text-sm">
        <p className="text-muted-foreground">{scheme.description}</p>

        <div className="space-y-3 rounded-lg border border-border bg-muted/30 p-4">
          <EligibilityRow
            icon={Calendar}
            label="Age"
            value={
              eligibility.minAge != null && eligibility.maxAge != null
                ? `${eligibility.minAge} - ${eligibility.maxAge}`
                : "No Age Restriction"
            }
          />

          <EligibilityRow
            icon={Wallet}
            label="Max Family Income"
            value={
              eligibility.maxFamilyIncome != null
                ? `₹${eligibility.maxFamilyIncome.toLocaleString()}`
                : "No Income Limit"
            }
          />

          <EligibilityRow
            icon={Users}
            label="Categories"
            value={
              (eligibility.requiredCategories ?? []).map(formatEnum).join(", ") ||
              "Any"
            }
          />

          <EligibilityRow
            icon={Briefcase}
            label="Occupations"
            value={
              (eligibility.allowedOccupations ?? []).map(formatEnum).join(", ") ||
              "Any"
            }
          />

          <EligibilityRow
            icon={GraduationCap}
            label="Student"
            value={
              eligibility.requiresStudent == null
                ? "Not Required"
                : eligibility.requiresStudent
                ? "Yes"
                : "No"
            }
          />

          <EligibilityRow
            icon={Sprout}
            label="Farmer"
            value={
              eligibility.requiresFarmer == null
                ? "Not Required"
                : eligibility.requiresFarmer
                ? "Yes"
                : "No"
            }
          />

          <EligibilityRow
            icon={Accessibility}
            label="Disability"
            value={
              eligibility.requiresDisability == null
                ? "Not Required"
                : eligibility.requiresDisability
                ? "Yes"
                : "No"
            }
          />
        </div>

        <div className="mt-auto pt-2">
          <Button
            className="w-full"
            onClick={handleApply}
            disabled={alreadyApplied || isPending}
          >
            {alreadyApplied
              ? "Already Applied"
              : isPending
              ? "Applying..."
              : "Apply"}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
