"use client";
import { useEffect, useSyncExternalStore } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/useAuthStore";
const subscribe = (callback: () => void) =>
  useAuthStore.persist.onFinishHydration(callback);
export function AuthGate({ children }: { children: React.ReactNode }) {
  const hydrated = useSyncExternalStore(
    subscribe,
    () => useAuthStore.persist.hasHydrated(),
    () => false,
  );
  const authenticated = useAuthStore((s) => s.isAuthenticated);
  const router = useRouter();
  useEffect(() => {
    if (hydrated && !authenticated) router.replace("/login");
  }, [hydrated, authenticated, router]);
  if (!hydrated || !authenticated)
    return (
      <div
        role="status"
        className="flex min-h-screen items-center justify-center text-muted-foreground"
      >
        Opening your workspace…
      </div>
    );
  return children;
}
