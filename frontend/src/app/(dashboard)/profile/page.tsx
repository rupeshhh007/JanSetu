"use client";

import { ProfileForm } from "@/features/citizen/components/ProfileForm";
import { useProfile } from "@/features/citizen/hooks/useProfile";

export default function ProfilePage() {
  const { data, isLoading, isError } = useProfile();

  if (isLoading) {
    return (
      <p className="noticeboard-kicker text-text-muted" role="status">
        Opening your profile…
      </p>
    );
  }

  if (isError) {
    return (
      <div
        role="alert"
        className="border-2 border-destructive bg-surface p-6 text-destructive shadow-[4px_4px_0_#c52626]"
      >
        We could not load your profile. Refresh to try again.
      </div>
    );
  }

  return <ProfileForm initialData={data ?? null} />;
}
