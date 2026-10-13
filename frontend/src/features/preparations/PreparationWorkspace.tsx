"use client";

import Link from "next/link";
import { CheckCircle2, CircleAlert, Clock3, ExternalLink, FileCheck2, HelpCircle, ShieldCheck } from "lucide-react";
import { useReportExternalApplication, useUpdatePreparationTask } from "./hooks";
import type { ApplicationPreparation, PreparationTaskStatus } from "./types";

export function PreparationWorkspace({ preparation, compact = false }: { preparation: ApplicationPreparation; compact?: boolean }) {
  const update = useUpdatePreparationTask();
  const report = useReportExternalApplication();
  const ready = preparation.status === "JANSETU_PREPARATION_READY";
  const reported = preparation.status === "CITIZEN_REPORTED_APPLICATION";
  return <article className="paper-card overflow-hidden">
    <div className="flex flex-col gap-4 border-b p-5 sm:flex-row sm:items-center sm:justify-between sm:p-6">
      <div className="min-w-0">
        <p className="text-[10px] font-semibold uppercase tracking-[.16em] text-primary">Application preparation</p>
        <h2 className="editorial mt-1 text-2xl">{preparation.schemeName}</h2>
        <p className="mt-2 text-xs text-muted-foreground">{statusCopy(preparation.status)}</p>
      </div>
      <div className="min-w-40">
        <div className="flex justify-between text-xs"><span>Preparation</span><strong>{preparation.progressPercent}%</strong></div>
        <div className="mt-2 h-2 overflow-hidden rounded-full bg-muted" role="progressbar" aria-label="Preparation completeness" aria-valuemin={0} aria-valuemax={100} aria-valuenow={preparation.progressPercent}>
          <div className="h-full rounded-full bg-primary transition-[width]" style={{ width: `${preparation.progressPercent}%` }} />
        </div>
        <p className="mt-2 text-[11px] text-muted-foreground">{preparation.completedTasks} of {preparation.totalTasks} requirements complete</p>
      </div>
    </div>
    {!compact && <div className="divide-y px-5 sm:px-6">
      {preparation.tasks.map((task) => <div key={task.id} className="py-4">
        <div className="flex items-start gap-3">
          {task.citizenCompletable ? <input type="checkbox" className="mt-1 size-4 accent-primary" checked={task.status === "COMPLETE"}
            disabled={update.isPending || reported} aria-label={`Mark ${task.title} ready`}
            onChange={(event) => update.mutate({ sessionId: preparation.id, taskId: task.id, complete: event.target.checked })} />
            : <TaskIcon status={task.status} />}
          <div className="min-w-0 flex-1"><div className="flex flex-wrap items-center gap-2"><strong className="text-sm">{task.title}</strong><TaskBadge status={task.status} /></div>
            {task.guidance && <p className="mt-1 text-xs leading-5 text-muted-foreground">{task.guidance}</p>}
            {task.citizenCompletable && <p className="mt-1 text-[11px] text-muted-foreground">Self-reported readiness only; JanSetu does not verify possession of this document.</p>}
            {(task.type === "PROFILE_FACT" || (task.type === "ELIGIBILITY" && task.status !== "COMPLETE")) && <Link href="/profile" className="text-link mt-2 text-xs">Review profile information</Link>}
            {!!task.evidenceReferences.length && <details className="mt-2"><summary className="cursor-pointer text-xs text-primary">Evidence & source</summary>
              <ul className="mt-2 space-y-2">{task.evidenceReferences.map((evidence, index) => <li key={`${evidence.snapshotId}-${index}`} className="rounded-lg bg-muted/60 p-3 text-xs leading-5">
                <span>{evidence.sourceTitle || "Recorded source"}{!evidence.snapshotId && <span className="ml-2 text-muted-foreground">· Link only, not an evidence snapshot</span>}</span>{evidence.excerpt && <blockquote className="mt-1 border-l-2 border-primary pl-2">{evidence.excerpt}</blockquote>}
                {evidence.sourceUrl && <a href={evidence.sourceUrl} target="_blank" rel="noopener noreferrer" className="text-link mt-1">Open source <ExternalLink size={12} /></a>}
              </li>)}</ul>
            </details>}
          </div>
        </div>
      </div>)}
    </div>}
    <div className="border-t bg-muted/30 p-5 sm:p-6">
      <div className="flex items-start gap-3 text-xs leading-5 text-muted-foreground"><ShieldCheck className="mt-0.5 size-4 shrink-0 text-primary" /><p>JanSetu records preparation only. No documents are uploaded, and official application status remains unknown without an authorized government integration.</p></div>
      <div className="mt-4 flex flex-col gap-2 sm:flex-row">
        {compact && <Link href={`/schemes/${preparation.schemeId}`} className="min-h-11 flex-1 rounded-lg border bg-white px-4 py-3 text-center text-sm font-semibold">Continue preparation</Link>}
        {preparation.officialUrl && <a href={preparation.officialUrl} target="_blank" rel="noopener noreferrer" className="action-link flex-1 justify-center">Open official portal <ExternalLink size={14} /></a>}
        {ready && <button type="button" disabled={report.isPending} onClick={() => window.confirm("Confirm that you applied on the official portal? JanSetu cannot verify submission or approval.") && report.mutate(preparation.id)} className="min-h-11 flex-1 rounded-lg border bg-white px-4 text-sm font-semibold disabled:opacity-50">I applied on the official portal</button>}
      </div>
      {reported && <p className="mt-3 flex items-center gap-2 text-xs font-semibold text-primary"><FileCheck2 size={15} />Citizen-reported application recorded {preparation.citizenReportedAppliedAt ? new Date(preparation.citizenReportedAppliedAt).toLocaleDateString("en-IN") : ""}</p>}
      {!compact && preparation.history.length > 0 && <details className="mt-4 border-t pt-4"><summary className="flex cursor-pointer items-center gap-2 text-xs font-semibold"><Clock3 size={14} />Preparation history ({preparation.history.length})</summary>
        <ol className="mt-3 space-y-2 text-xs text-muted-foreground">{preparation.history.map((item) => <li key={item.id}>{humanize(item.eventType)} · {new Date(item.recordedAt).toLocaleString("en-IN")}</li>)}</ol>
      </details>}
    </div>
  </article>;
}

function TaskIcon({ status }: { status: PreparationTaskStatus }) {
  return status === "COMPLETE" ? <CheckCircle2 className="mt-0.5 size-4 shrink-0 text-primary" /> : status === "UNKNOWN" ? <HelpCircle className="mt-0.5 size-4 shrink-0 text-warning" /> : <CircleAlert className="mt-0.5 size-4 shrink-0 text-warning" />;
}
function TaskBadge({ status }: { status: PreparationTaskStatus }) {
  const style = status === "COMPLETE" ? "bg-accent text-primary" : status === "UNKNOWN" ? "bg-slate-100 text-slate-700" : "bg-amber-50 text-amber-900";
  return <span className={`rounded px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide ${style}`}>{status.toLowerCase()}</span>;
}
function statusCopy(status: ApplicationPreparation["status"]) {
  if (status === "JANSETU_PREPARATION_READY") return "Your recorded preparation tasks are complete. Verify everything again on the official portal.";
  if (status === "CITIZEN_REPORTED_APPLICATION") return "You reported applying externally. JanSetu has no verified official status.";
  return "Continue the known tasks below. Unknown requirements must be checked with the official source.";
}
function humanize(value: string) { return value.toLowerCase().replaceAll("_", " ").replace(/^./, (letter) => letter.toUpperCase()); }
