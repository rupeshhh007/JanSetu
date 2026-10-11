"use client";
import Link from "next/link";
import { ArrowRight, ScanLine } from "lucide-react";
import { RecommendationGrid } from "@/features/recommendations/components/RecommendationGrid";
import { RecommendationSkeleton } from "@/features/recommendations/components/RecommendationSkeleton";
import { useRecommendations } from "@/features/recommendations/hooks/useRecommendations";
import { useProfile } from "@/features/citizen/hooks/useProfile";
export default function RecommendationsPage() {
  const profile = useProfile();
  const query = useRecommendations(Boolean(profile.data));
  const recommendations = query.data ?? [];
  const readyCount = recommendations.filter(
    (recommendation) => recommendation.recommendationState === "READY_TO_REVIEW",
  ).length;
  const profileActionCount = recommendations.filter(
    (recommendation) => recommendation.recommendationState === "PROFILE_INFORMATION_NEEDED",
  ).length;
  const policyReviewCount = recommendations.filter(
    (recommendation) => recommendation.recommendationState === "POLICY_INFORMATION_UNAVAILABLE",
  ).length;
  return (
    <div className="space-y-7">
      <div>
        <h1 className="editorial text-4xl sm:text-5xl">
          A shortlist you can verify.
        </h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
          Results use your saved profile and recorded scheme requirements. We
          show what is supported by your information, what is still needed, and
          the next action. Final eligibility is determined by the official department.
        </p>
      </div>
      {profile.isLoading || query.isLoading ? (
        <RecommendationSkeleton />
      ) : profile.isError || query.isError ? (
        <div role="alert" className="paper-card p-8">
          We couldn’t check your matches.{" "}
          <button
            className="text-link"
            onClick={() => {
              void profile.refetch();
              void query.refetch();
            }}
          >
            Try again
          </button>
        </div>
      ) : !profile.data ? (
        <section className="paper-card p-10 text-center">
          <ScanLine className="mx-auto text-primary" size={32} />
          <h2 className="editorial mt-5 text-3xl">
            A little context goes a long way.
          </h2>
          <p className="mx-auto mt-3 max-w-md text-sm text-muted-foreground">
            Create your profile so JanSetu can check the recorded age, income,
            occupation, and location requirements for you.
          </p>
          <Link href="/profile" className="action-link mt-6">
            Create my profile <ArrowRight size={16} />
          </Link>
        </section>
      ) : !recommendations.length ? (
        <section className="paper-card p-10 text-center">
          <h2 className="editorial text-3xl">No recorded matches just yet.</h2>
          <p className="mx-auto mt-3 max-w-md text-sm text-muted-foreground">
            Your profile is complete, but none of the current catalogue’s
            recorded rules match. This does not mean you are ineligible for all
            government support.
          </p>
          <Link className="action-link mt-6" href="/schemes">
            Explore the catalogue <ArrowRight size={16} />
          </Link>
          <Link className="text-link ml-4 mt-4" href="/profile">
            Review profile
          </Link>
        </section>
      ) : (
        <>
          <div className="flex flex-wrap items-center gap-x-5 gap-y-2 border-y py-4 text-sm">
            <p className="font-medium text-primary">
              {readyCount} {readyCount === 1 ? "scheme is" : "schemes are"} ready to review
            </p>
            {profileActionCount > 0 ? (
              <p className="text-warning">
                {profileActionCount} {profileActionCount === 1 ? "needs" : "need"} more profile information
              </p>
            ) : null}
            {policyReviewCount > 0 ? (
              <p className="text-muted-foreground">
                {policyReviewCount} {policyReviewCount === 1 ? "scheme needs" : "schemes need"} policy details recorded
              </p>
            ) : null}
          </div>
          <RecommendationGrid recommendations={recommendations} />
        </>
      )}
    </div>
  );
}
