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

export default function DashboardPage() {
  const { data: profile } = useProfile();
  const { data: schemes = [] } = useSchemes();
  const { data: recommendations = [] } = useRecommendations();
  const { data: applications = [] } = useApplications();

  const pendingApplications = applications.filter(
    (application) =>
      application.status === "SUBMITTED" || application.status === "UNDER_REVIEW"
  ).length;

  return (
    <div className="relative min-h-screen pb-24">
      {/* Premium Ambient Background (macOS Sonoma / Apple Intelligence style) */}
      <div className="fixed inset-0 -z-10 overflow-hidden pointer-events-none bg-[#F5F5F7]">
        <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-blue-500/10 blur-[120px]" />
        <div className="absolute bottom-[20%] right-[-10%] w-[40%] h-[50%] rounded-full bg-purple-500/10 blur-[120px]" />
        <div className="absolute top-[40%] left-[20%] w-[30%] h-[30%] rounded-full bg-emerald-500/5 blur-[100px]" />
      </div>

      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 space-y-10 pt-10">
        <DashboardHero firstName={profile?.firstName} />

        {/* 4-Column Grid for Desktop (Widgets) */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
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
            caption={recommendations.length > 0 ? "Matched to you" : "Complete profile"}
            captionTone={recommendations.length > 0 ? "positive" : "neutral"}
          />
          <DashboardStatCard
            title="Applications"
            value={applications.length}
            icon={ClipboardList}
            accent="neutral"
            caption={pendingApplications > 0 ? `${pendingApplications} pending` : "Caught up"}
            captionTone={pendingApplications > 0 ? "neutral" : "positive"}
          />
          <DashboardStatCard
            title="Profile"
            value={profile ? "Done" : "Pending"}
            icon={UserCircle2}
            accent={profile ? "success" : "neutral"}
            caption={profile ? "Completed" : "Not started"}
            captionTone={profile ? "positive" : "neutral"}
          />
        </div>

        {/* 2/3 and 1/3 Split Layout for Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column (Wider) */}
          <div className="lg:col-span-2 space-y-8">
            <RecentActivity applications={applications} />
            <div className="pt-2">
              <TopRecommendationPreview recommendations={recommendations} />
            </div>
          </div>

          {/* Right Column (Narrower) */}
          <div className="space-y-8">
            <QuickActions />
            <ProfileCompletionCard profile={profile ?? undefined} />
          </div>
        </div>
      </div>
    </div>
  );
}
