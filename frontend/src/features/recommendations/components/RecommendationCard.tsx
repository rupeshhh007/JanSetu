import Link from "next/link";
import {
  ArrowRight,
  CheckCircle2,
  CircleAlert,
  ClipboardPenLine,
  ShieldCheck,
} from "lucide-react";
import type { RecommendationResponse } from "../types";
export function RecommendationCard({
  recommendation,
}: {
  recommendation: RecommendationResponse;
}) {
  const { scheme, matchedCriteria, missingInformation, evidenceScore } = recommendation;
  const needsProfileInformation = recommendation.requiresProfileAction;
  const policyInformationUnavailable =
    recommendation.recommendationState === "POLICY_INFORMATION_UNAVAILABLE";
  const isReady = recommendation.recommendationState === "READY_TO_REVIEW";
  const reasons = isReady ? matchedCriteria : missingInformation;
  const stateLabel = isReady
    ? "Recorded requirements met"
    : needsProfileInformation
      ? "Profile information needed"
      : "Policy details not recorded";
  return (
    <article className="flex h-full flex-col rounded-xl border bg-white p-6 shadow-[var(--shadow-card)] transition-shadow hover:shadow-[var(--shadow-card-hover)]">
      <div
        className={`mb-4 flex items-center gap-2 text-xs font-semibold ${isReady ? "text-primary" : "text-warning"}`}
      >
        {isReady ? (
          <ShieldCheck size={16} />
        ) : (
          <CircleAlert size={16} />
        )}
        {stateLabel}
      </div>
      <div className="flex items-start justify-between gap-4">
        <h2 className="text-lg leading-snug">{scheme.name}</h2>
        <div className="shrink-0 text-right">
          <p className="text-xl font-semibold tabular-nums text-primary">{evidenceScore}%</p>
          <p className="text-[0.65rem] font-medium uppercase tracking-wide text-muted-foreground">
            recorded evidence
          </p>
        </div>
      </div>
      <div
        className="mt-3 h-1.5 overflow-hidden rounded-full bg-muted"
        role="progressbar"
        aria-label={`${evidenceScore}% of the recorded criteria have supporting profile information`}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={evidenceScore}
      >
        <div
          className="h-full bg-primary transition-[width] duration-300"
          style={{ width: `${evidenceScore}%` }}
        />
      </div>
      <p className="mt-3 text-sm leading-6 text-muted-foreground">
        {scheme.description}
      </p>
      <h3 className="mb-3 mt-5 text-sm">
        {isReady ? "Why this appears" : needsProfileInformation ? "What to add" : "What to check"}
      </h3>
      <ul
        className="mb-6 flex-1 space-y-3"
        aria-label={isReady ? "Recorded requirements met" : stateLabel}
      >
        {reasons.map((reason) => (
          <li key={reason} className="flex gap-2 text-xs leading-5">
            {isReady ? (
              <CheckCircle2 size={15} className="mt-0.5 shrink-0 text-primary" />
            ) : (
              <CircleAlert size={15} className="mt-0.5 shrink-0 text-warning" />
            )}
            {reason}
          </li>
        ))}
      </ul>
      <p className="border-t pt-4 text-xs leading-5 text-muted-foreground">
        {recommendation.nextStep} This is a recorded-policy check, not an official approval decision.
      </p>
      <Link
        href={needsProfileInformation ? "/profile" : `/schemes/${scheme.id}`}
        className="text-link mt-4"
      >
        {needsProfileInformation ? <ClipboardPenLine size={16} /> : null}
        {needsProfileInformation
          ? "Complete profile"
          : policyInformationUnavailable
            ? "Review official details"
            : "Review details & next steps"}
        <ArrowRight size={16} />
      </Link>
    </article>
  );
}
