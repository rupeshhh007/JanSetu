"use client";

import { ProfileForm } from "@/features/citizen/components/ProfileForm";
import { useProfile } from "@/features/citizen/hooks/useProfile";

export default function ProfilePage() {
  const { data, isLoading, isError } = useProfile();

  if (isLoading) {
    return (
      <p className="text-muted-foreground">Loading profile...</p>
    );
  }

  if (isError) {
    return (
      <p className="text-destructive">Failed to load profile.</p>
    );
  }

  return <ProfileForm initialData={data ?? null} />;
}