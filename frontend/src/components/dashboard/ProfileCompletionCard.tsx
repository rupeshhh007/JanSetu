"use client";

import Link from "next/link";
import { CheckCircle2, Circle, ArrowRight } from "lucide-react";
import { cn } from "@/lib/utils";
import type { ProfileResponse } from "@/features/citizen/types";

interface ProfileCompletionCardProps {
  profile: ProfileResponse | undefined;
}

interface CompletionGroup {
  label: string;
  complete: boolean;
}

function getCompletionGroups(profile: ProfileResponse | undefined): CompletionGroup[] {
  if (!profile) {
    return [
      { label: "Personal details", complete: false },
      { label: "Location", complete: false },
      { label: "Category & status", complete: false },
      { label: "Occupation & income", complete: false },
    ];
  }
  return [
    { label: "Personal details", complete: Boolean(profile.firstName && profile.lastName && profile.dateOfBirth && profile.gender) },
    { label: "Location", complete: Boolean(profile.state && profile.district && profile.pincode) },
    { label: "Category & status", complete: Boolean(profile.category && profile.maritalStatus) },
    { label: "Occupation & income", complete: profile.primaryOccupation != null && profile.annualFamilyIncome != null },
  ];
}

export function ProfileCompletionCard({ profile }: ProfileCompletionCardProps) {
  const groups = getCompletionGroups(profile);
  const completedCount = groups.filter((g) => g.complete).length;
  const percentage = Math.round((completedCount / groups.length) * 100);

  const radius = 38;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (percentage / 100) * circumference;

  return (
    <div className="flex flex-col gap-4">
      <h2 className="text-sm font-semibold tracking-wide text-foreground/80 pl-2">
        Profile Status
      </h2>
      <div className="rounded-[2rem] bg-white/60 backdrop-blur-xl border border-white/60 shadow-[0_8px_30px_rgb(0,0,0,0.04)] p-6 transition-transform duration-300 hover:scale-[1.02]">
        
        <div className="flex items-center gap-6">
          <div className="relative flex size-24 shrink-0 items-center justify-center">
            <svg viewBox="0 0 100 100" className="size-24 -rotate-90 drop-shadow-sm" aria-hidden="true">
              <circle cx="50" cy="50" r={radius} strokeWidth="8" className="fill-none stroke-black/5" />
              <circle
                cx="50" cy="50" r={radius} strokeWidth="8" strokeLinecap="round"
                strokeDasharray={circumference} strokeDashoffset={offset}
                className={cn(
                  "fill-none stroke-primary transition-all duration-1000 ease-out",
                  percentage === 100 && "stroke-success"
                )}
              />
            </svg>
            <span className="absolute text-xl font-bold tabular-nums text-foreground tracking-tight">
              {percentage}%
            </span>
          </div>

          <ul className="min-w-0 flex-1 space-y-2.5">
            {groups.map((group) => (
              <li key={group.label} className="flex items-center gap-2.5 text-[14px]">
                {group.complete ? (
                  <CheckCircle2 className="size-[18px] shrink-0 text-success drop-shadow-sm" />
                ) : (
                  <Circle className="size-[18px] shrink-0 text-muted-foreground/30" />
                )}
                <span className={cn("truncate font-medium", group.complete ? "text-foreground" : "text-muted-foreground")}>
                  {group.label}
                </span>
              </li>
            ))}
          </ul>
        </div>

        <Link
          href="/profile"
          className="mt-6 flex items-center justify-center gap-1.5 w-full rounded-2xl bg-white border border-black/5 py-3 text-[15px] font-semibold text-primary hover:bg-gray-50 transition-colors shadow-sm"
        >
          {percentage === 100 ? "View profile" : "Complete your profile"}
          <ArrowRight className="size-4" aria-hidden="true" />
        </Link>
      </div>
    </div>
  );
}
