"use client";
import { use } from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowLeft,
  ExternalLink,
  Bookmark,
  CheckCircle2,
  CircleAlert,
  Printer,
  ArrowRight,
  MessageCircle,
} from "lucide-react";
import { useSchemes } from "@/features/schemes/hooks/useSchemes";
import { getSchemeEligibility } from "@/features/schemes/api/schemesApi";
import { useApplications } from "@/features/applications/hooks/useApplications";
import { useApplyToScheme } from "@/features/applications/hooks/useApplyToScheme";
import {
  useDocumentChecklist,
  useSavedSchemes,
  useWorkspaceStore,
} from "@/store/useWorkspaceStore";
export default function SchemeDetail({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const catalogue = useSchemes();
  const scheme = catalogue.data?.find((s) => s.id === id);
  const eligibility = useQuery({
    queryKey: ["eligibility", id],
    queryFn: () => getSchemeEligibility(id),
    enabled: !!scheme,
  });
  const apps = useApplications();
  const apply = useApplyToScheme();
  const saved = useSavedSchemes().includes(id);
  const checked = useDocumentChecklist(id);
  const { toggleSaved, toggleDocument } = useWorkspaceStore();
  const active = apps.data?.some(
    (a) => a.schemeId === id && !["REJECTED", "WITHDRAWN"].includes(a.status),
  );
  if (catalogue.isPending) return <p role="status">Loading scheme…</p>;
  if (catalogue.isError)
    return (
      <div role="alert">
        Couldn’t load the scheme.{" "}
        <button className="text-link" onClick={() => catalogue.refetch()}>
          Try again
        </button>
      </div>
    );
  if (!scheme)
    return (
      <div>
        <h1 className="editorial text-3xl">
          This scheme is no longer available.
        </h1>
        <Link className="text-link mt-5" href="/schemes">
          Return to catalogue
        </Link>
      </div>
    );
  const evaluation = eligibility.data;
  const ready = scheme.requiredDocuments.filter((d) =>
    checked.includes(d),
  ).length;
  return (
    <div className="space-y-7 pb-10">
      <Link href="/schemes" className="text-link no-print">
        <ArrowLeft size={16} />
        All schemes
      </Link>
      <div>
        <p className="mb-3 text-sm text-primary">
          {scheme.state} · {scheme.department}
        </p>
        <h1 className="editorial max-w-4xl text-4xl sm:text-5xl">
          {scheme.name}
        </h1>
        <p className="mt-4 max-w-3xl leading-7 text-muted-foreground">
          {scheme.description}
        </p>
        <div className="no-print mt-5 flex flex-wrap gap-3">
          <button
            className="flex min-h-10 items-center gap-2 rounded-lg border bg-white px-4 text-sm"
            aria-pressed={saved}
            onClick={() => toggleSaved(id)}
          >
            <Bookmark size={16} fill={saved ? "currentColor" : "none"} />
            {saved ? "Saved" : "Save scheme"}
          </button>
          <button
            className="flex min-h-10 items-center gap-2 rounded-lg border bg-white px-4 text-sm"
            onClick={() => window.print()}
          >
            <Printer size={16} />
            Print checklist
          </button>
          <button
            className="text-link"
            onClick={() =>
              window.dispatchEvent(
                new CustomEvent("jansetu:guide", {
                  detail: {
                    message: `Tell me about ${scheme.name}`,
                    schemeId: id,
                  },
                }),
              )
            }
          >
            <MessageCircle size={16} />
            Ask the guide
          </button>
        </div>
      </div>
      <div className="grid items-start gap-6 xl:grid-cols-[1.45fr_1fr]">
        <div className="space-y-6">
          <section className="paper-card p-6">
            <h2 className="editorial text-2xl">What this scheme offers</h2>
            {scheme.benefits.length ? (
              <ul className="mt-5 space-y-4">
                {scheme.benefits.map((b) => (
                  <li key={b} className="flex gap-3 text-sm leading-6">
                    <CheckCircle2
                      size={18}
                      className="mt-1 shrink-0 text-primary"
                    />
                    {b}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="mt-3 text-sm text-muted-foreground">
                Benefits have not been recorded. Check the official source.
              </p>
            )}
          </section>
          <section className="paper-card p-6">
            <div className="flex items-center justify-between gap-3">
              <h2 className="editorial text-2xl">Your document checklist</h2>
              <span className="text-xs text-primary">
                {ready}/{scheme.requiredDocuments.length} ready
              </span>
            </div>
            <p className="mt-2 text-xs leading-5 text-muted-foreground">
              Mark what you have ready. This checklist stays on this device; no
              documents are uploaded.
            </p>
            <div className="mt-5 divide-y">
              {scheme.requiredDocuments.map((document) => (
                <label
                  key={document}
                  className="flex cursor-pointer items-start gap-3 py-4 text-sm"
                >
                  <input
                    className="mt-1 size-4 accent-primary"
                    type="checkbox"
                    checked={checked.includes(document)}
                    onChange={() => toggleDocument(id, document)}
                  />
                  <span>{document}</span>
                </label>
              ))}
            </div>
            {!scheme.requiredDocuments.length && (
              <p className="mt-4 text-sm">
                No document list is recorded. Contact the official department.
              </p>
            )}
          </section>
          <section className="paper-card p-6">
            <h2 className="editorial text-2xl">Source & freshness</h2>
            <p className="mt-3 text-sm">
              {scheme.sourceName || "Source not recorded"}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">
              Catalogue verification date:{" "}
              {scheme.lastVerifiedAt
                ? new Date(scheme.lastVerifiedAt).toLocaleDateString("en-IN")
                : "Not recorded"}
              . Requirements may change; verify before applying.
            </p>
            {scheme.officialUrl && (
              <a
                href={scheme.officialUrl}
                target="_blank"
                rel="noreferrer"
                className="text-link mt-4"
              >
                Visit official website <ExternalLink size={15} />
              </a>
            )}
          </section>
        </div>
        <div className="space-y-5">
          <section className="paper-card p-6">
            <h2 className="editorial text-2xl">How you match</h2>
            {eligibility.isPending ? (
              <p role="status" className="mt-4 text-sm">
                Checking recorded requirements…
              </p>
            ) : eligibility.isError ? (
              <div role="alert" className="mt-4 text-sm">
                Eligibility check unavailable.{" "}
                <button
                  className="text-link"
                  onClick={() => eligibility.refetch()}
                >
                  Retry
                </button>
              </div>
            ) : (
              evaluation && (
                <>
                  <div
                    className={`mt-4 rounded-lg p-3 text-sm font-medium ${evaluation.outcome === "ELIGIBLE" ? "bg-accent text-primary" : "bg-amber-50 text-amber-900"}`}
                  >
                    {evaluation.outcome === "ELIGIBLE"
                      ? "You meet the recorded requirements"
                      : evaluation.outcome === "NEEDS_INFORMATION"
                        ? "More information is needed"
                        : "Some requirements are not met"}
                  </div>
                  <ul className="mt-4 space-y-3">
                    {evaluation.matchedCriteria.map((reason) => (
                      <li key={reason} className="flex gap-2 text-xs leading-5">
                        <CheckCircle2
                          size={15}
                          className="mt-0.5 shrink-0 text-primary"
                        />
                        {reason}
                      </li>
                    ))}
                    {[
                      ...evaluation.unmetCriteria,
                      ...evaluation.missingInformation,
                    ].map((reason) => (
                      <li key={reason} className="flex gap-2 text-xs leading-5">
                        <CircleAlert
                          size={15}
                          className="mt-0.5 shrink-0 text-warning"
                        />
                        {reason}
                      </li>
                    ))}
                  </ul>
                  <Link href="/profile" className="text-link mt-4">
                    Review my profile <ArrowRight size={14} />
                  </Link>
                </>
              )
            )}
            <p className="mt-5 border-t pt-4 text-xs leading-5 text-muted-foreground">
              Based on your saved profile and{" "}
              {evaluation?.evaluatedRuleCount ?? "the"} recorded checks. The
              official department determines final eligibility.
            </p>
          </section>
          <section className="rounded-xl bg-[#163d32] p-6 text-white">
            <h2 className="editorial text-2xl">Take the next step</h2>
            <p className="mt-3 text-sm leading-6 text-white/75">
              Create a JanSetu tracking record for preparation and local review.
              This does not submit an application to the government.
            </p>
            {apps.isError && (
              <p role="alert" className="mt-3 text-sm">
                Existing records are unavailable.{" "}
                <button className="underline" onClick={() => apps.refetch()}>
                  Retry
                </button>
              </p>
            )}
            <button
              disabled={
                active ||
                apply.isPending ||
                apps.isPending ||
                apps.isError ||
                evaluation?.outcome !== "ELIGIBLE"
              }
              onClick={() => apply.mutate({ schemeId: id })}
              className="no-print mt-5 min-h-11 w-full rounded-lg bg-white px-4 text-sm font-semibold text-[#163d32] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {active
                ? "Already in your applications"
                : apply.isPending
                  ? "Creating record…"
                  : "Start tracking this scheme"}
            </button>
            {active && (
              <Link
                className="mt-3 inline-flex text-sm text-white underline"
                href="/applications"
              >
                View your record
              </Link>
            )}
            {scheme.officialUrl && (
              <a
                href={scheme.officialUrl}
                target="_blank"
                rel="noreferrer"
                className="mt-4 flex items-center justify-center gap-2 text-sm text-white"
              >
                Continue on official portal <ExternalLink size={14} />
              </a>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}
