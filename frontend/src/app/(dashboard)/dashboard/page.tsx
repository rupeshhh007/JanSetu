"use client";

import { FileText, Sparkles, ClipboardList, UserCircle2 } from "lucide-react";

import { DashboardStatCard } from "@/components/ui/DashboardStatcard";
import { QuickActions } from "@/components/dashboard/QuickActions";
import { RecentActivity } from "@/components/dashboard/RecentActivity";

import { useSchemes } from "@/features/schemes/hooks/useSchemes";
import { useApplications } from "@/features/applications/hooks/useApplications";
import { useRecommendations } from "@/features/recommendations/hooks/useRecommendations";
import { useProfile } from "@/features/citizen/hooks/useProfile";

export default function DashboardPage() {
  const { data: profile } = useProfile();
  const { data: schemes = [] } = useSchemes();
  const { data: recommendations = [] } = useRecommendations();
  const { data: applications = [] } = useApplications();

  return (
    <div className="space-y-10">
      {/* Welcome hero */}
      <div className="relative overflow-hidden rounded-2xl border border-border bg-card px-6 py-8 sm:px-8 sm:py-10">
        <div
          className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/[0.06] via-transparent to-transparent"
          aria-hidden="true"
        />
        <div
          className="pointer-events-none absolute -right-24 -top-24 size-72 rounded-full bg-primary/10 blur-3xl"
          aria-hidden="true"
        />

        <div className="relative">
          <h1 className="text-3xl font-bold tracking-tight">
            Welcome{profile?.firstName ? `, ${profile.firstName}` : ""} 👋
          </h1>

          <p className="mt-2 max-w-xl text-muted-foreground">
            Here&apos;s an overview of your JanSetu account — schemes, matches,
            and applications, all in one place.
          </p>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardStatCard
          title="Available Schemes"
          value={schemes.length}
          icon={FileText}
          accent="primary"
        />

        <DashboardStatCard
          title="Recommendations"
          value={recommendations.length}
          icon={Sparkles}
          accent="info"
        />

        <DashboardStatCard
          title="Applications"
          value={applications.length}
          icon={ClipboardList}
          accent="warning"
        />

        <DashboardStatCard
          title="Profile Status"
          value={profile ? "Completed" : "Pending"}
          icon={UserCircle2}
          accent="success"
        />
      </div>

      {/* Quick actions */}
      <QuickActions />

      {/* Recent activity */}
      <div className="grid gap-6 xl:grid-cols-3">
        <div className="xl:col-span-2">
          <RecentActivity applications={applications} />
        </div>

        <div className="rounded-2xl border border-dashed border-border bg-muted/30 p-6">
          <h3 className="text-sm font-semibold text-foreground">
            Tip
          </h3>
          <p className="mt-2 text-sm text-muted-foreground">
            Complete your profile to unlock personalized scheme
            recommendations tailored to your eligibility.
          </p>
        </div>
      </div>
    </div>
  );
}
