"use client";
import { useState } from "react";
import Link from "next/link";
import { ArrowRight, ClipboardList, Search } from "lucide-react";
import { ApplicationGrid } from "@/features/applications/components/ApplicationGrid";
import { ApplicationSkeleton } from "@/features/applications/components/ApplicationSkeleton";
import { useApplications } from "@/features/applications/hooks/useApplications";
import { usePreparations } from "@/features/preparations/hooks";
import { PreparationWorkspace } from "@/features/preparations/PreparationWorkspace";
export default function ApplicationsPage() {
  const query = useApplications();
  const preparations = usePreparations();
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("ALL");
  const applications = [...(query.data ?? [])]
    .filter(
      (a) =>
        (status === "ALL" || a.status === status) &&
        a.schemeName.toLowerCase().includes(search.toLowerCase()),
    )
    .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt));
  return (
    <div className="space-y-7">
      <div>
        <h1 className="editorial text-4xl sm:text-5xl">
          Every next step, in one place.
        </h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-muted-foreground">
          Track your JanSetu records, review notes, and status history. These
          are local preparation records, separate from applications on official
          government portals.
        </p>
      </div>
      {!!query.data?.length && (
        <div className="flex flex-wrap items-center gap-3 rounded-xl border bg-white p-4">
          <Search size={17} />
          <input
            className="min-h-10 min-w-0 flex-1 bg-transparent text-sm"
            aria-label="Search applications"
            placeholder="Find a scheme in your applications…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select
            aria-label="Filter application status"
            className="min-h-10 rounded-lg border bg-white px-3 text-sm"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            {[
              "ALL",
              "SUBMITTED",
              "UNDER_REVIEW",
              "APPROVED",
              "REJECTED",
              "WITHDRAWN",
            ].map((s) => (
              <option value={s} key={s}>
                {s === "ALL"
                  ? "All statuses"
                  : s.replaceAll("_", " ").toLowerCase()}
              </option>
            ))}
          </select>
        </div>
      )}
      {preparations.isPending ? <ApplicationSkeleton /> : preparations.isError ? <div role="alert" className="paper-card p-8">Couldn’t load preparation journeys. <button className="text-link" onClick={() => preparations.refetch()}>Try again</button></div>
        : preparations.data?.length ? <section aria-labelledby="preparation-journeys-heading"><div className="mb-4"><p className="text-xs font-semibold uppercase tracking-[.16em] text-primary">Persisted preparation</p><h2 id="preparation-journeys-heading" className="editorial mt-1 text-3xl">Your preparation journeys</h2></div><div className="grid gap-5 lg:grid-cols-2">{preparations.data.map((preparation) => <PreparationWorkspace key={preparation.id} preparation={preparation} compact />)}</div></section> : null}
      {(preparations.data?.length ?? 0) > 0 && <div className="border-t pt-6"><h2 className="editorial text-2xl">Legacy tracked applications</h2><p className="mt-2 text-xs text-muted-foreground">Older JanSetu review records remain available below.</p></div>}
      {query.isPending ? (
        <ApplicationSkeleton />
      ) : query.isError ? (
        <div role="alert" className="paper-card p-8">
          Couldn’t load applications.{" "}
          <button className="text-link" onClick={() => query.refetch()}>
            Try again
          </button>
        </div>
      ) : query.isSuccess && preparations.isSuccess && !query.data?.length && !preparations.data?.length ? (
        <div className="paper-card px-6 py-16 text-center">
          <ClipboardList
            size={34}
            className="mx-auto text-primary"
            strokeWidth={1.5}
          />
          <h2 className="editorial mt-5 text-3xl">
            Your journey starts with a scheme.
          </h2>
          <p className="mt-3 text-sm text-muted-foreground">
            Find relevant support, prepare your documents, and start tracking.
          </p>
          <Link href="/schemes" className="action-link mt-6">
            Explore schemes <ArrowRight size={16} />
          </Link>
        </div>
      ) : !query.data?.length ? null : !applications.length ? (
        <p className="paper-card p-10 text-center text-muted-foreground">
          No applications match these filters.
        </p>
      ) : (
        <ApplicationGrid applications={applications} />
      )}
    </div>
  );
}
