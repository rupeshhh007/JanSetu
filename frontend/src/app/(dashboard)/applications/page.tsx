"use client";

import { ApplicationGrid } from "@/features/applications/components/ApplicationGrid";
import { ApplicationSkeleton } from "@/features/applications/components/ApplicationSkeleton";
import { EmptyApplications } from "@/features/applications/components/EmptyApplications";
import { useApplications } from "@/features/applications/hooks/useApplications";

export default function ApplicationsPage() {
  const {
    data: applications,
    isLoading,
    isError,
  } = useApplications();

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">
          My Applications
        </h1>

        <ApplicationSkeleton />
      </div>
    );
  }

  if (isError) {
    return (
      <p className="text-destructive">
        Failed to load applications.
      </p>
    );
  }

  if (!applications?.length) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">
          My Applications
        </h1>

        <EmptyApplications />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">
          My Applications
        </h1>

        <p className="text-muted-foreground">
          Track the status of your submitted applications.
        </p>
      </div>

      <ApplicationGrid applications={applications} />
    </div>
  );
}