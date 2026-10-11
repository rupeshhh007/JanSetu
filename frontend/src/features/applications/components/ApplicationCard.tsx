import Link from "next/link";
import { ArrowRight, Clock3 } from "lucide-react";
import type { ApplicationResponse } from "../types";
import { ApplicationStatusBadge } from "./ApplicationStatusBadge";
import { APPLICATION_STATUS_META } from "@/lib/statusMeta";
export function ApplicationCard({
  application,
}: {
  application: ApplicationResponse;
}) {
  return (
    <article className="flex h-full flex-col rounded-xl border bg-white p-6">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <span className="text-[10px] uppercase tracking-wider text-muted-foreground">
          Record {application.applicationId.slice(0, 8)}
        </span>
        <ApplicationStatusBadge status={application.status} />
      </div>
      <h2 className="text-lg">{application.schemeName}</h2>
      <p className="mt-3 text-xs text-muted-foreground">
        Started {new Date(application.submittedAt).toLocaleDateString("en-IN")}
      </p>
      <div className="my-5 rounded-lg bg-muted/60 p-4">
        <p className="mb-1 text-xs font-semibold">Latest review note</p>
        <p className="text-sm leading-6 text-muted-foreground">
          {application.remarks ||
            "Your record is waiting for a JanSetu administrator to review it."}
        </p>
      </div>
      <details className="mb-5 border-t pt-4">
        <summary className="flex cursor-pointer items-center gap-2 text-sm font-medium">
          <Clock3 size={16} />
          Status history ({application.history?.length ?? 0})
        </summary>
        <ol className="ml-2 mt-4 space-y-4 border-l pl-5">
          {application.history?.map((item) => (
            <li key={item.id} className="text-xs">
              <p className="font-medium">
                {APPLICATION_STATUS_META[item.newStatus].label}
              </p>
              <p className="mt-1 text-muted-foreground">
                {new Date(item.changedAt).toLocaleString("en-IN")}
              </p>
              {item.remarks && <p className="mt-1 leading-5">{item.remarks}</p>}
            </li>
          ))}
        </ol>
      </details>
      <Link
        className="text-link mt-auto"
        href={`/schemes/${application.schemeId}`}
      >
        Scheme details & checklist <ArrowRight size={15} />
      </Link>
    </article>
  );
}
