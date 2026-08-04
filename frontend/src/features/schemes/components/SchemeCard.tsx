"use client";

import { ArrowRight, CheckCircle2, Loader2 } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

import { useApplyToScheme } from "@/features/applications/hooks/useApplyToScheme";
import { useApplications } from "@/features/applications/hooks/useApplications";

import { deriveSchemeCategory, type SchemeCategory } from "@/features/schemes/lib/deriveSchemeCateogory";
import { EligibilityBadges } from "./EligibilityBadge"
import type { SchemeResponse } from "../types";

interface SchemeCardProps {
  scheme: SchemeResponse;
}

const ACCENT_STYLES: Record<
  SchemeCategory["accent"],
  { icon: string; ring: string; badge: string; glow: string }
> = {
  primary: {
    icon: "bg-gradient-to-br from-blue-500/15 via-blue-500/10 to-transparent text-primary",
    ring: "ring-primary/15",
    badge: "border-primary/20 bg-primary/5 text-primary",
    glow: "from-primary/10",
  },
  success: {
    icon: "bg-gradient-to-br from-emerald-500/15 via-emerald-500/10 to-transparent text-success",
    ring: "ring-success/15",
    badge: "border-success/20 bg-success/5 text-success",
    glow: "from-success/10",
  },
  neutral: {
    icon: "bg-gradient-to-br from-slate-500/10 via-slate-500/5 to-transparent text-foreground/70",
    ring: "ring-border",
    badge: "border-border bg-muted/50 text-muted-foreground",
    glow: "from-slate-500/5",
  },
};

export function SchemeCard({ scheme }: SchemeCardProps) {
  const category = deriveSchemeCategory(scheme.name);
  const styles = ACCENT_STYLES[category.accent];

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
      className={cn(
        "group relative flex h-full flex-col overflow-hidden border border-border transition-all duration-300 ease-out",
        "hover:-translate-y-1 hover:border-foreground/15 hover:shadow-[var(--shadow-card-hover)]"
      )}
    >
      {/* corner glow — decorative only */}
      <div
        className={cn(
          "pointer-events-none absolute -right-10 -top-10 size-32 rounded-full bg-gradient-to-br to-transparent opacity-0 blur-2xl transition-opacity duration-300 group-hover:opacity-70",
          styles.glow
        )}
        aria-hidden="true"
      />

      <CardHeader className="relative gap-3">
        <div className="flex items-start justify-between gap-3">
          <div
            className={cn(
              "flex size-11 shrink-0 items-center justify-center rounded-xl ring-1 transition-transform duration-300 ease-out group-hover:scale-105 group-hover:-rotate-3",
              styles.icon,
              styles.ring
            )}
          >
            <category.icon className="size-5" strokeWidth={2} aria-hidden="true" />
          </div>

          <span
            className={cn(
              "inline-flex shrink-0 items-center rounded-full border px-2.5 py-1 text-xs font-medium",
              styles.badge
            )}
          >
            {category.label}
          </span>
        </div>

        <CardTitle className="leading-snug">{scheme.name}</CardTitle>
      </CardHeader>

      <CardContent className="relative flex flex-1 flex-col gap-4">
        <p className="line-clamp-3 text-sm text-muted-foreground">
          {scheme.description}
        </p>

        <EligibilityBadges eligibility={scheme.eligibilityCriteria} />

        <Button
          className="mt-auto h-12 w-full gap-2 rounded-xl px-4 text-base font-semibold"
          onClick={handleApply}
          disabled={alreadyApplied || isPending}
        >
          {alreadyApplied ? (
            <>
              <CheckCircle2 className="size-4.5" aria-hidden="true" />
              Already Applied
            </>
          ) : isPending ? (
            <>
              <Loader2 className="size-4.5 animate-spin" aria-hidden="true" />
              Applying...
            </>
          ) : (
            <>
              Apply Now
              <ArrowRight className="size-4.5" aria-hidden="true" />
            </>
          )}
        </Button>
      </CardContent>
    </Card>
  );
}
