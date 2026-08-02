"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import { useApplyToScheme } from "@/features/applications/hooks/useApplyToScheme";
import { useApplications } from "@/features/applications/hooks/useApplications";

import type { RecommendationResponse } from "../types";

interface RecommendationCardProps {
  recommendation: RecommendationResponse;
}

export function RecommendationCard({
  recommendation,
}: RecommendationCardProps) {
  const { scheme, matchPercentage, matchedCriteria } = recommendation;

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

  const badgeColor =
    matchPercentage >= 90
      ? "bg-green-100 text-green-700"
      : matchPercentage >= 75
      ? "bg-yellow-100 text-yellow-700"
      : "bg-red-100 text-red-700";

  return (
    <Card className="flex h-full flex-col transition-shadow hover:shadow-lg">
      <CardHeader className="space-y-3">
        <div className="flex items-center justify-between">
          <CardTitle>{scheme.name}</CardTitle>

          <span
            className={`rounded-full px-3 py-1 text-xs font-semibold ${badgeColor}`}
          >
            {matchPercentage}% Match
          </span>
        </div>

        <p className="text-sm text-muted-foreground">
          {scheme.description}
        </p>
      </CardHeader>

      <CardContent className="flex flex-1 flex-col space-y-3">
        <h4 className="font-medium">Why you're eligible</h4>

        <ul className="space-y-2 flex-1">
          {matchedCriteria.map((criterion) => (
            <li
              key={criterion}
              className="flex items-start gap-2 text-sm"
            >
              <span className="text-green-600">✓</span>
              <span>{criterion}</span>
            </li>
          ))}
        </ul>

        <Button
          className="mt-auto w-full"
          disabled={alreadyApplied || isPending}
          onClick={handleApply}
        >
          {alreadyApplied
            ? "Already Applied"
            : isPending
            ? "Applying..."
            : "Apply"}
        </Button>
      </CardContent>
    </Card>
  );
}