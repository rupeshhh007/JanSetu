"use client";
import Link from "next/link";
import { ArrowRight, ScanLine } from "lucide-react";
import { RecommendationGrid } from "@/features/recommendations/components/RecommendationGrid";
import { RecommendationSkeleton } from "@/features/recommendations/components/RecommendationSkeleton";
import { useRecommendations } from "@/features/recommendations/hooks/useRecommendations";
import { useProfile } from "@/features/citizen/hooks/useProfile";
export default function RecommendationsPage() {
  const profile = useProfile();
  const query = useRecommendations();
  return (
    <div className="space-y-7">
      <div>
        <h1 className="editorial text-4xl sm:text-5xl">
          Support, with you in mind.
        </h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
          Matches come from your saved profile and the catalogue’s recorded
          rules. Every result explains why it appears. Final eligibility is
          determined by the official department.
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
      ) : !query.data?.length ? (
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
          <p className="text-sm text-primary">
            {query.data.length} schemes meet the recorded requirements
          </p>
          <RecommendationGrid recommendations={query.data} />
        </>
      )}
    </div>
  );
}
