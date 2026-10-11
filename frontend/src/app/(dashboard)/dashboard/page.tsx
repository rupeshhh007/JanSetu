"use client";
import Link from "next/link";
import {
  ArrowRight,
  Check,
  FileText,
  ScanLine,
  ShieldCheck,
  MessageCircle,
} from "lucide-react";
import { useSchemes } from "@/features/schemes/hooks/useSchemes";
import { useApplications } from "@/features/applications/hooks/useApplications";
import { useProfile } from "@/features/citizen/hooks/useProfile";
import { useSavedSchemes } from "@/store/useWorkspaceStore";
import { APPLICATION_STATUS_META } from "@/lib/statusMeta";
export default function DashboardPage() {
  const profile = useProfile();
  const schemes = useSchemes();
  const applications = useApplications();
  const saved = useSavedSchemes();
  const steps = [
    {
      title: "Create your profile",
      description: "Tell us about your circumstances to get better matches.",
      href: "/profile",
      done: !!profile.data,
    },
    {
      title: "Find a relevant scheme",
      description: "Explore schemes that may support your needs.",
      href: "/schemes",
      done: saved.length > 0,
    },
    {
      title: "Prepare your documents",
      description: "Open a scheme for its document checklist and next steps.",
      href: "/schemes",
      done: (applications.data?.length ?? 0) > 0,
    },
  ];
  return (
    <div className="space-y-7 pb-10">
      <div className="flex flex-wrap items-start justify-between gap-5">
        <div>
          <h1 className="editorial text-4xl sm:text-[46px] xl:text-[50px]">
            A clearer path to support.
          </h1>
          <p className="mt-3 max-w-2xl text-sm leading-7 text-muted-foreground">
            Discover relevant schemes, prepare your documents, and keep track of
            your next steps.
          </p>
        </div>
        <Link className="action-link sm:mt-2" href="/schemes">
          Browse schemes <ArrowRight size={17} />
        </Link>
      </div>
      <section className="relative overflow-hidden rounded-xl bg-[#163d32] px-7 py-7 text-white sm:px-8">
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -bottom-44 -right-14 size-96 rounded-full border border-white/20 p-6"
        >
          <div className="h-full rounded-full border border-white/20 p-6">
            <div className="h-full rounded-full border border-white/20 p-6">
              <div className="h-full rounded-full border border-white/20" />
            </div>
          </div>
        </div>
        <div className="relative max-w-xl">
          <h2 className="editorial text-3xl">
            {profile.data
              ? `Your next chapter, ${profile.data.firstName}.`
              : "Your next step starts with you."}
          </h2>
          <p className="mt-3 text-sm text-white/80">
            {profile.data
              ? "Your profile is ready. Explore your matches and see exactly which recorded requirements you meet."
              : "Complete your citizen profile to see schemes matched to your circumstances."}
          </p>
          <Link
            href={profile.data ? "/recommendations" : "/profile"}
            className="mt-5 inline-flex min-h-11 items-center gap-5 rounded-lg bg-white px-5 text-sm font-semibold text-[#163d32] hover:bg-accent"
          >
            {profile.data ? "Explore my matches" : "Complete profile"}
            <ArrowRight size={17} />
          </Link>
        </div>
      </section>
      <div className="grid grid-cols-3 divide-x border-y py-5 text-center">
        {[
          {
            title: "Available schemes",
            value: schemes.data?.length,
            loading: schemes.isPending,
            error: schemes.isError,
            href: "/schemes",
          },
          {
            title: "Saved schemes",
            value: saved.length,
            loading: false,
            error: false,
            href: "/schemes?saved=1",
          },
          {
            title: "Applications",
            value: applications.data?.length,
            loading: applications.isPending,
            error: applications.isError,
            href: "/applications",
          },
        ].map((item) => (
          <Link href={item.href} key={item.title} className="group px-2">
            <p className="text-xs text-muted-foreground sm:text-sm">
              {item.title}
            </p>
            <p className="mt-1 text-2xl font-semibold text-primary group-hover:underline">
              {item.loading
                ? "—"
                : item.error
                  ? "Unavailable"
                  : (item.value ?? 0)}
            </p>
          </Link>
        ))}
      </div>
      <div className="grid gap-5 xl:grid-cols-[1.5fr_1fr]">
        <section className="rounded-xl border bg-white p-6">
          <div className="flex items-center justify-between gap-3">
            <h2 className="editorial text-2xl">Your applications</h2>
            <Link className="text-link" href="/applications">
              View all <ArrowRight size={15} />
            </Link>
          </div>
          {applications.isError ? (
            <div role="alert" className="py-12 text-sm text-muted-foreground">
              Application data is unavailable.{" "}
              <button
                className="text-link"
                onClick={() => applications.refetch()}
              >
                Try again
              </button>
            </div>
          ) : applications.isPending ? (
            <p role="status" className="py-12 text-muted-foreground">
              Loading applications…
            </p>
          ) : applications.data?.length ? (
            <div className="mt-5 divide-y">
              {[...applications.data]
                .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
                .slice(0, 4)
                .map((item) => (
                  <Link
                    href="/applications"
                    key={item.applicationId}
                    className="flex items-center gap-4 py-4"
                  >
                    <FileText className="shrink-0 text-primary" size={23} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">
                        {item.schemeName}
                      </p>
                      <p className="mt-1 text-xs text-muted-foreground">
                        {APPLICATION_STATUS_META[item.status].label} ·{" "}
                        {new Date(item.updatedAt).toLocaleDateString("en-IN")}
                      </p>
                    </div>
                    <ArrowRight size={15} />
                  </Link>
                ))}
            </div>
          ) : (
            <div className="flex min-h-64 flex-col items-center justify-center py-8 text-center">
              <span className="mb-5 flex size-16 items-center justify-center rounded-full bg-muted">
                <FileText
                  size={30}
                  strokeWidth={1.2}
                  className="text-primary/60"
                />
              </span>
              <h3 className="editorial text-2xl">Nothing to track yet</h3>
              <p className="mt-2 text-sm text-muted-foreground">
                Explore schemes and start your preparation here.
              </p>
              <Link className="text-link mt-5" href="/schemes">
                Browse schemes <ArrowRight size={15} />
              </Link>
            </div>
          )}
        </section>
        <section className="rounded-xl border bg-white p-6">
          <h2 className="editorial text-2xl">Your preparation checklist</h2>
          <div className="mt-4 divide-y">
            {steps.map((step, i) => (
              <Link
                href={step.href}
                key={step.title}
                className="flex gap-4 py-4"
              >
                <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-accent text-sm text-primary">
                  {step.done ? <Check size={17} /> : i + 1}
                </span>
                <div>
                  <h3 className="text-sm">{step.title}</h3>
                  <p className="mt-1 text-xs leading-5 text-muted-foreground">
                    {step.description}
                  </p>
                </div>
              </Link>
            ))}
          </div>
          <button
            className="text-link mt-3"
            onClick={() => window.dispatchEvent(new Event("jansetu:guide"))}
          >
            A guide when you need it <ArrowRight size={15} />
          </button>
        </section>
      </div>
      <section>
        <h2 className="editorial text-3xl">Built around your next step</h2>
        <div className="mt-5 grid gap-4 sm:grid-cols-3 sm:divide-x">
          <Link href="/recommendations" className="text-link">
            <ScanLine size={17} />
            Profile-based matches <ArrowRight size={15} />
          </Link>
          <Link href="/schemes" className="text-link sm:justify-center">
            <ShieldCheck size={17} />
            Source-backed information
          </Link>
          <button
            className="text-link sm:justify-center"
            onClick={() => window.dispatchEvent(new Event("jansetu:guide"))}
          >
            <MessageCircle size={17} />A guide when you need it
          </button>
        </div>
      </section>
    </div>
  );
}
