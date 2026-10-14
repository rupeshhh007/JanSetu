"use client";

import { useState } from "react";
import Link from "next/link";
import { Download, LockKeyhole, ShieldCheck, ExternalLink } from "lucide-react";
import { toast } from "sonner";
import { downloadAccountData } from "@/features/privacy/downloadAccountData";
import { ChangePasswordForm } from "@/features/account/ChangePasswordForm";
import { getErrorMessage } from "@/lib/api/errorHandler";

export default function SettingsPage() {
  const [exporting, setExporting] = useState(false);

  async function handleDownload() {
    setExporting(true);
    try {
      await downloadAccountData();
      toast.success("Your account export is ready. Keep this file private.");
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setExporting(false);
    }
  }

  return (
    <div className="mx-auto max-w-3xl space-y-7 pb-16">
      <div>
        <p className="text-xs font-medium uppercase tracking-widest text-primary">Your account</p>
        <h1 className="editorial mt-2 text-4xl">Privacy &amp; data</h1>
        <p className="mt-3 text-sm leading-7 text-muted-foreground">
          Your information should stay understandable and under your control.
          JanSetu is an independent discovery and preparation service, not a government portal.
        </p>
      </div>

      <section className="rounded-xl border bg-white p-6">
        <div className="flex items-center gap-3">
          <Download size={22} className="text-primary" aria-hidden="true" />
          <h2 className="editorial text-2xl">Download my data</h2>
        </div>
        <p className="mt-3 text-sm leading-6 text-muted-foreground">
          Download your profile, declared facts and consent settings, saved schemes,
          recorded assessments, preparation journeys, and in-app notifications in JSON format.
          The file can contain sensitive personal information.
        </p>
        <button
          type="button"
          onClick={handleDownload}
          disabled={exporting}
          aria-busy={exporting}
          className="mt-5 inline-flex min-h-11 items-center gap-2 rounded-lg bg-[#163d32] px-5 text-sm font-semibold text-white disabled:opacity-60"
        >
          <Download size={17} aria-hidden="true" />
          {exporting ? "Preparing export…" : "Download JSON export"}
        </button>
        <p role="status" aria-live="polite" className="mt-3 text-xs text-muted-foreground">
          This download is prepared for your signed-in account only.
        </p>
      </section>

      <section className="rounded-xl border bg-white p-6">
        <div className="flex items-center gap-3">
          <LockKeyhole size={22} className="text-primary" aria-hidden="true" />
          <h2 className="editorial text-2xl">Your sign-in session</h2>
        </div>
        <p className="mt-3 text-sm leading-6 text-muted-foreground">
          Your current login is limited to this browser tab. Sign out before sharing a device.
          Passwords are never included in downloaded data.
        </p>
        <Link href="/profile" className="text-link mt-4">Review my profile</Link>
      </section>

      <ChangePasswordForm />

      <section className="rounded-xl border bg-white p-6">
        <div className="flex items-center gap-3">
          <ShieldCheck size={22} className="text-primary" aria-hidden="true" />
          <h2 className="editorial text-2xl">How JanSetu uses your information</h2>
        </div>
        <p className="mt-3 text-sm leading-6 text-muted-foreground">
          Your profile and recorded facts help evaluate scheme rules. JanSetu does not
          submit applications to official government portals, and any locally reported
          application status is not an official status verification.
        </p>
        <Link href="/privacy" className="text-link mt-4">
          Read the privacy notice <ExternalLink size={15} aria-hidden="true" />
        </Link>
      </section>
    </div>
  );
}
