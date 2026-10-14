"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { KeyRound } from "lucide-react";
import { changeAccountPassword } from "./passwordApi";
import { getErrorMessage } from "@/lib/api/errorHandler";
import { useAuthStore } from "@/store/useAuthStore";

export function ChangePasswordForm() {
  const router = useRouter();
  const logout = useAuthStore((s) => s.logout);
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirm, setConfirm] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (next !== confirm) {
      toast.error("New passwords do not match.");
      return;
    }
    if (next.length < 12 || next.length > 72 || new TextEncoder().encode(next).length > 72) {
      toast.error("Use a password of 12 to 72 UTF-8 bytes.");
      return;
    }
    setSubmitting(true);
    try {
      await changeAccountPassword(current, next);
      setCurrent("");
      setNext("");
      setConfirm("");
      logout();
      toast.success("Password changed. All earlier sign-in sessions have been revoked.");
      router.replace("/login");
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="rounded-xl border bg-white p-6">
      <div className="flex items-center gap-3">
        <KeyRound size={22} className="text-primary" aria-hidden="true" />
        <h2 className="editorial text-2xl">Change password</h2>
      </div>
      <p className="mt-3 text-sm leading-6 text-muted-foreground">
        Confirm your current password to replace it. You will need to sign in again on all devices.
      </p>
      <form className="mt-5 space-y-4" onSubmit={submit}>
        <label className="block text-sm">
          <span className="mb-1 block">Current password</span>
          <input type="password" required autoComplete="current-password" value={current}
            onChange={(e) => setCurrent(e.target.value)}
            className="h-11 w-full rounded-lg border px-3" />
        </label>
        <label className="block text-sm">
          <span className="mb-1 block">New password (12+ characters)</span>
          <input type="password" required minLength={12} maxLength={72}
            autoComplete="new-password" value={next} onChange={(e) => setNext(e.target.value)}
            className="h-11 w-full rounded-lg border px-3" />
        </label>
        <label className="block text-sm">
          <span className="mb-1 block">Confirm new password</span>
          <input type="password" required autoComplete="new-password" value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            className="h-11 w-full rounded-lg border px-3" />
        </label>
        <button type="submit" disabled={submitting}
          className="min-h-11 rounded-lg bg-[#163d32] px-5 text-sm font-semibold text-white disabled:opacity-60">
          {submitting ? "Updating…" : "Change password"}
        </button>
      </form>
    </section>
  );
}
