"use client";

import { FileText, Sparkles, ClipboardList, UserCircle2 } from "lucide-react";

import { DashboardStatCard } from "@/components/ui/DashboardStatcard";
import { DashboardHero } from "@/components/dashboard/DashboardHero";
import { QuickActions } from "@/components/dashboard/QuickActions";
import { RecentActivity } from "@/components/dashboard/RecentActivity";
import { ProfileCompletionCard } from "@/components/dashboard/ProfileCompletionCard";
import { TopRecommendationPreview } from "@/components/dashboard/TopRecommendationPreview";

import { useSchemes } from "@/features/schemes/hooks/useSchemes";
import { useApplications } from "@/features/applications/hooks/useApplications";
import { useRecommendations } from "@/features/recommendations/hooks/useRecommendations";
import { useProfile } from "@/features/citizen/hooks/useProfile";

import { formatRelativeTime } from "@/lib/utils";

export default function DashboardPage() {
  const { data: profile } = useProfile();
  const { data: schemes = [] } = useSchemes();
  const { data: recommendations = [] } = useRecommendations();
  const { data: applications = [] } = useApplications();

  // Pure presentational derivation from data already returned by useApplications().
  // Not a new API call, not new business logic — just counting an array we already have.
  const pendingApplications = applications.filter(
    (application) =>
      application.status === "SUBMITTED" || application.status === "UNDER_REVIEW"
  ).length;

  return (
    <div className="space-y-12">
      {/* Hero greeting */}
      <DashboardHero firstName={profile?.firstName} />

      {/* Stat cards */}
      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardStatCard
          title="Available Schemes"
          value={schemes.length}
          icon={FileText}
          accent="primary"
          caption="Open to explore"
        />

        <DashboardStatCard
          title="Recommendations"
          value={recommendations.length}
          icon={Sparkles}
          accent="success"
          caption={
            recommendations.length > 0
              ? "Matched to your profile"
              : "Complete your profile for matches"
          }
          captionTone={recommendations.length > 0 ? "positive" : "neutral"}
        />

        <DashboardStatCard
          title="Applications"
          value={applications.length}
          icon={ClipboardList}
          accent="neutral"
          caption={
            pendingApplications > 0
              ? `${pendingApplications} pending review`
              : "All caught up"
          }
          captionTone={pendingApplications > 0 ? "neutral" : "positive"}
        />

        <DashboardStatCard
          title="Profile Status"
          value={profile ? "Completed" : "Pending"}
          icon={UserCircle2}
          accent={profile ? "success" : "neutral"}
          caption={
            profile ? `Updated ${formatRelativeTime(profile.lastUpdated)}` : "Not started yet"
          }
          captionTone={profile ? "positive" : "neutral"}
        />
      </div>

      {/* Quick actions */}
      <QuickActions />

      {/* Recent activity + side rail */}
      <div className="grid gap-6 xl:grid-cols-3">
        <div className="xl:col-span-2">
          <RecentActivity applications={applications} />
        </div>

        <div className="space-y-6">
          <ProfileCompletionCard
  profile={profile ?? undefined}
/>
          <TopRecommendationPreview recommendations={recommendations} />
        </div>
      </div>
    </div>
  );
}
