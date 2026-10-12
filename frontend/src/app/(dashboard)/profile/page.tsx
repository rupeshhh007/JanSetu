"use client";

import { ProfileForm } from "@/features/citizen/components/ProfileForm";
import { ProfileFactLedger } from "@/features/citizen/components/ProfileFactLedger";
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

  return (
    <div className="space-y-6">
      <ProfileForm initialData={data ?? null} />
      <ProfileFactLedger enabled={Boolean(data)} />
    </div>
  );
}
