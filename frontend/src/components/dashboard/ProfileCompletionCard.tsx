"use client";

import Link from "next/link";
import { CheckCircle2, Circle, ArrowRight } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";

import type { ProfileResponse } from "@/features/citizen/types";

interface ProfileCompletionCardProps {
  /** Comes straight from the existing useProfile() query result — untouched. */
  profile: ProfileResponse | undefined;
}

interface CompletionGroup {
  label: string;
  complete: boolean;
}

/**
 * Pure presentational derivation from already-fetched profile data.
 * Does not call any API, hook, or validation logic — just groups fields
 * that already exist on ProfileResponse into readable checklist rows.
 */
function getCompletionGroups(
  profile: ProfileResponse | undefined
): CompletionGroup[] {
  if (!profile) {
    return [
      { label: "Personal details", complete: false },
      { label: "Location", complete: false },
      { label: "Category & marital status", complete: false },
      { label: "Occupation & income", complete: false },
    ];
  }

  return [
    {
      label: "Personal details",
      complete: Boolean(
        profile.firstName && profile.lastName && profile.dateOfBirth && profile.gender
      ),
    },
    {
      label: "Location",
      complete: Boolean(profile.state && profile.district && profile.pincode),
    },
    {
      label: "Category & marital status",
      complete: Boolean(profile.category && profile.maritalStatus),
    },
    {
      label: "Occupation & income",
      complete:
        profile.primaryOccupation != null && profile.annualFamilyIncome != null,
    },
  ];
}

export function ProfileCompletionCard({ profile }: ProfileCompletionCardProps) {
  const groups = getCompletionGroups(profile);
  const completedCount = groups.filter((group) => group.complete).length;
  const percentage = Math.round((completedCount / groups.length) * 100);

  const radius = 34;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (percentage / 100) * circumference;

  return (
    <Card
      className={cn(
        "border border-border transition-all duration-300 ease-out",
        "hover:-translate-y-1 hover:border-foreground/15 hover:shadow-[var(--shadow-card-hover)]"
      )}
    >
      <CardHeader className="pb-2">
        <CardTitle className="text-base">Profile completion</CardTitle>
      </CardHeader>

      <CardContent className="pt-3">
        <div className="flex items-center gap-5">
          <div className="relative flex size-20 shrink-0 items-center justify-center">
            <svg viewBox="0 0 80 80" className="size-20 -rotate-90" aria-hidden="true">
              <circle
                cx="40"
                cy="40"
                r={radius}
                strokeWidth="7"
                className="fill-none stroke-muted"
              />
              <circle
                cx="40"
                cy="40"
                r={radius}
                strokeWidth="7"
                strokeLinecap="round"
                strokeDasharray={circumference}
                strokeDashoffset={offset}
                className={cn(
                  "fill-none stroke-primary transition-[stroke-dashoffset] duration-500 ease-out",
                  percentage === 100 && "stroke-success"
                )}
              />
            </svg>
            <span className="absolute text-lg font-semibold tabular-nums text-foreground">
              {percentage}%
            </span>
          </div>

          <ul className="min-w-0 flex-1 space-y-1.5">
            {groups.map((group) => (
              <li key={group.label} className="flex items-center gap-2 text-sm">
                {group.complete ? (
                  <CheckCircle2
                    className="size-4 shrink-0 text-success"
                    aria-hidden="true"
                  />
                ) : (
                  <Circle
                    className="size-4 shrink-0 text-muted-foreground/40"
                    aria-hidden="true"
                  />
                )}
                <span
                  className={cn(
                    "truncate",
                    group.complete ? "text-foreground" : "text-muted-foreground"
                  )}
                >
                  {group.label}
                </span>
              </li>
            ))}
          </ul>
        </div>

        <Link
          href="/profile"
          className="mt-5 flex items-center gap-1 rounded-md text-sm font-medium text-primary hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        >
          {percentage === 100 ? "View profile" : "Complete your profile"}
          <ArrowRight className="size-3.5" aria-hidden="true" />
        </Link>
      </CardContent>
    </Card>
  );
}
