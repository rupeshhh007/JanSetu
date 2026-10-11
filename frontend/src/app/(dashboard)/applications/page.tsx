"use client";
import { useState } from "react";
import Link from "next/link";
import { ArrowRight, ClipboardList, Search } from "lucide-react";
import { ApplicationGrid } from "@/features/applications/components/ApplicationGrid";
import { ApplicationSkeleton } from "@/features/applications/components/ApplicationSkeleton";
import { useApplications } from "@/features/applications/hooks/useApplications";
export default function ApplicationsPage() {
  const query = useApplications();
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
      {query.isPending ? (
        <ApplicationSkeleton />
      ) : query.isError ? (
        <div role="alert" className="paper-card p-8">
          Couldn’t load applications.{" "}
          <button className="text-link" onClick={() => query.refetch()}>
            Try again
          </button>
        </div>
      ) : !query.data?.length ? (
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
      ) : !applications.length ? (
        <p className="paper-card p-10 text-center text-muted-foreground">
          No applications match these filters.
        </p>
      ) : (
        <ApplicationGrid applications={applications} />
      )}
    </div>
  );
}
