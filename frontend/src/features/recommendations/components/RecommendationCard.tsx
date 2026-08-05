"use client";

import { useState } from "react";

import ReactMarkdown from "react-markdown";
import { Sparkles } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

import { useApplyToScheme } from "@/features/applications/hooks/useApplyToScheme";
import { useApplications } from "@/features/applications/hooks/useApplications";
import { useExplainEligibility } from "@/features/ai/hooks/useExplainEligibility";
import { useProfile } from "@/features/citizen/hooks/useProfile";

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
  const { data: profile } = useProfile();

  const {
    mutateAsync: explain,
    isPending: isGenerating,
  } = useExplainEligibility();

  const [explanation, setExplanation] = useState("");

  const alreadyApplied = applications.some(
    (application) => application.schemeId === scheme.id
  );

  function handleApply() {
    mutate({
      schemeId: scheme.id,
    });
  }

  async function handleExplain() {
    if (explanation) {
      setExplanation("");
      return;
    }

    const profileSummary = [
      profile?.dateOfBirth
        ? `Born ${profile.dateOfBirth}`
        : null,
      profile?.primaryOccupation,
      profile?.category,
      profile?.annualFamilyIncome != null
        ? `Income ₹${profile.annualFamilyIncome}`
        : null,
      profile?.isStudent ? "Student" : null,
      profile?.isFarmer ? "Farmer" : null,
      profile?.hasDisability ? "Person with Disability" : null,
    ]
      .filter(Boolean)
      .join(", ");

    const response = await explain({
      userProfileSummary: profileSummary,
      schemeName: scheme.name,
      schemeDescription: scheme.description,
      matchedCriteria,
    });

    setExplanation(response.explanation);
  }

  const badgeColor =
    matchPercentage >= 90
      ? "bg-green-100 text-green-700"
      : matchPercentage >= 75
      ? "bg-yellow-100 text-yellow-700"
      : "bg-red-100 text-red-700";

  return (
    <Card className="flex h-full flex-col transition-all duration-300 hover:-translate-y-1 hover:shadow-xl">
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

      <CardContent className="flex flex-1 flex-col space-y-4">
        <h4 className="font-medium">
          Why you <area shape="circle" coords="" href="" alt="" /> eligible
        </h4>

        <ul className="flex-1 space-y-2">
          {matchedCriteria.map((criterion) => (
            <li
              key={criterion}
              className="flex items-start gap-2 text-sm"
            >
              <span className="text-green-600">
                ✓
              </span>

              <span>{criterion}</span>
            </li>
          ))}
        </ul>

        <Button
          className="w-full"
          disabled={alreadyApplied || isPending}
          onClick={handleApply}
        >
          {alreadyApplied
            ? "Already Applied"
            : isPending
            ? "Applying..."
            : "Apply"}
        </Button>

        <Button
          variant="outline"
          className="w-full"
          disabled={isGenerating}
          onClick={handleExplain}
        >
          <Sparkles className="mr-2 h-4 w-4" />

          {isGenerating
            ? "Generating..."
            : explanation
            ? "Hide AI Explanation"
            : "✨ Explain with AI"}
        </Button>

        {explanation && (
          <div className="rounded-xl border bg-muted/40 p-4">
            <div className="mb-3 flex items-center gap-2 font-semibold text-primary">
              <Sparkles className="h-4 w-4" />
              AI Explanation
            </div>

            <div className="prose prose-sm max-w-none">
              <ReactMarkdown>
                {explanation}
              </ReactMarkdown>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}