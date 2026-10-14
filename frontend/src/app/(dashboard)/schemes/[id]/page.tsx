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
import { schemeSourceFreshness } from "@/features/schemes/sourceFreshness";
import { useSavedSchemeIds, useToggleSavedScheme } from "@/features/bookmarks/hooks";
import { usePreparations, useStartPreparation } from "@/features/preparations/hooks";
import { PreparationWorkspace } from "@/features/preparations/PreparationWorkspace";
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
  const preparations = usePreparations();
  const startPreparation = useStartPreparation();
  const savedQuery = useSavedSchemeIds();
  const saved = (savedQuery.data ?? []).includes(id);
  const toggleSaved = useToggleSavedScheme();
  const preparation = preparations.data?.find((item) => item.schemeId === id);
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
  const source = schemeSourceFreshness(scheme.lastVerifiedAt);
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
            disabled={toggleSaved.isPending || savedQuery.isPending || savedQuery.isError}
            onClick={() => toggleSaved.mutate({ schemeId: id, saved })}
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
          {preparation ? <PreparationWorkspace preparation={preparation} /> : <section className="paper-card p-6">
            <div className="flex items-center justify-between gap-3">
              <h2 className="editorial text-2xl">Known document requirements</h2>
              <span className="text-xs text-primary">
                {scheme.requiredDocuments.length} recorded
              </span>
            </div>
            <p className="mt-2 text-xs leading-5 text-muted-foreground">
              Start a preparation journey to save progress across sessions. JanSetu never asks you to upload these documents.
            </p>
            <ul className="mt-5 divide-y">
              {scheme.requiredDocuments.map((document) => (
                <li
                  key={document}
                  className="flex items-start gap-3 py-4 text-sm"
                >
                  <CircleAlert size={16} className="mt-0.5 shrink-0 text-warning" />
                  <span>{document}</span>
                </li>
              ))}
            </ul>
            {!scheme.requiredDocuments.length && (
              <p className="mt-4 text-sm">
                No document list is recorded. Contact the official department.
              </p>
            )}
          </section>}
          <section className="paper-card p-6">
            <h2 className="editorial text-2xl">Source & freshness</h2>
            <p className="mt-3 text-sm">
              {scheme.sourceName || "Source not recorded"}
            </p>
            <p className={`mt-2 text-sm ${source.status === "recent" ? "text-muted-foreground" : "text-amber-800"}`}>
              {source.text}. Always confirm details with the official department before applying.
            </p>
            {source.status !== "recent" && (
              <p className="mt-2 text-xs leading-5 text-muted-foreground">
                We cannot confirm whether benefits, deadlines, or document requirements have changed.
              </p>
            )}
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
            {preparations.isError && (
              <p role="alert" className="mt-3 text-sm">
                Preparation records are unavailable.{" "}
                <button className="underline" onClick={() => preparations.refetch()}>
                  Retry
                </button>
              </p>
            )}
            <button
              disabled={
                Boolean(preparation) || startPreparation.isPending || preparations.isPending || preparations.isError
              }
              onClick={() => startPreparation.mutate(id)}
              className="no-print mt-5 min-h-11 w-full rounded-lg bg-white px-4 text-sm font-semibold text-[#163d32] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {preparation ? "Preparation journey started" : startPreparation.isPending ? "Starting journey…" : "Start preparation journey"}
            </button>
            {preparation && (
              <Link
                className="mt-3 inline-flex text-sm text-white underline"
                href="/applications"
              >
                View all preparation journeys
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
