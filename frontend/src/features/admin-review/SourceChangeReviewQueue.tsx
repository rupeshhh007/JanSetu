"use client";

import { useState } from "react";
import axios from "axios";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, CheckCircle2, ExternalLink, History, Loader2, ShieldCheck, XCircle } from "lucide-react";
import { toast } from "sonner";
import type { SchemeResponse } from "@/features/schemes/types";
import {
  acceptSourceChange,
  getAdminAuditHistory,
  getPendingSourceChanges,
  getSourceChangeHistory,
  rejectSourceChange,
} from "./api";
import type { SourceChangeCandidate } from "./types";
import { PolicyPublicationWorkflow } from "./PolicyPublicationWorkflow";
import { AIInvestigationWorkspace } from "./AIInvestigationWorkspace";

interface Props { schemes: SchemeResponse[] }

export function SourceChangeReviewQueue({ schemes }: Props) {
  const client = useQueryClient();
  const [selected, setSelected] = useState<SourceChangeCandidate | null>(null);
  const [schemeId, setSchemeId] = useState("");
  const [reason, setReason] = useState("");
  const [conflictMessage, setConflictMessage] = useState<string | null>(null);
  const pending = useQuery({ queryKey: ["admin-source-changes", "pending"], queryFn: getPendingSourceChanges });
  const history = useQuery({ queryKey: ["admin-source-changes", "history"], queryFn: getSourceChangeHistory });
  const audit = useQuery({ queryKey: ["admin-audit"], queryFn: getAdminAuditHistory });

  const refresh = async () => {
    await Promise.all([
      client.invalidateQueries({ queryKey: ["admin-source-changes"] }),
      client.invalidateQueries({ queryKey: ["admin-audit"] }),
    ]);
    setSelected(null); setSchemeId(""); setReason(""); setConflictMessage(null);
  };
  const failure = (error: unknown) => {
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 409) {
      setConflictMessage("Another administrator completed this review first. The latest queue has been loaded.");
      toast.error("Another reviewer already decided this candidate.");
      void Promise.all([
        client.invalidateQueries({ queryKey: ["admin-source-changes"] }),
        client.invalidateQueries({ queryKey: ["admin-audit"] }),
      ]);
      return;
    }
    toast.error("The review decision could not be recorded.");
  };
  const accept = useMutation({
    mutationFn: () => acceptSourceChange(selected!.id, { schemeId, reason: reason.trim() }),
    onSuccess: async () => { toast.success("Evidence marked applicable. No policy was published or changed."); await refresh(); },
    onError: failure,
  });
  const reject = useMutation({
    mutationFn: () => rejectSourceChange(selected!.id, { reason: reason.trim() }),
    onSuccess: async () => { toast.success("Evidence change rejected for policy use."); await refresh(); },
    onError: failure,
  });
  const busy = accept.isPending || reject.isPending;
  const reviewed = history.data?.filter((candidate) => candidate.status !== "PENDING_REVIEW") ?? [];

  return (
    <section className="mt-12 border-t border-border pt-8" aria-labelledby="source-review-heading">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="noticeboard-kicker text-primary">03 / Policy evidence review</p>
          <h2 id="source-review-heading" className="mt-2 text-3xl">Source change queue</h2>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-text-muted">Review detected evidence changes and associate relevant evidence with a scheme. A decision here never publishes or edits a JanSetu policy.</p>
        </div>
        <div className="rounded-lg border border-border bg-surface px-4 py-3 text-center shadow-sm" aria-label={`${pending.data?.length ?? 0} pending source changes`}>
          <strong className="block text-2xl text-primary">{pending.data?.length ?? 0}</strong>
          <span className="text-xs font-bold uppercase tracking-wider text-text-muted">Pending</span>
        </div>
      </div>

      {pending.isLoading ? <State icon={<Loader2 className="size-5 animate-spin" />} text="Loading evidence review queue…" /> : pending.isError ? <State icon={<AlertTriangle className="size-5" />} text="The evidence queue could not be loaded. Refresh to try again." error /> : pending.data?.length ? (
        <div className="mt-6 grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(22rem,0.72fr)]">
          {conflictMessage && <p className="xl:col-span-2 rounded-lg border border-warning bg-warning/10 px-4 py-3 text-sm font-semibold" role="alert">{conflictMessage}</p>}
          <div className="space-y-3">
            {pending.data.map((candidate) => (
              <button key={candidate.id} type="button" onClick={() => { setSelected(candidate); setSchemeId(""); setReason(""); setConflictMessage(null); }}
                className={`w-full rounded-lg border p-5 text-left shadow-sm transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary ${selected?.id === candidate.id ? "border-primary bg-primary/5" : "border-border bg-surface hover:border-primary/50"}`}>
                <div className="flex flex-wrap items-start justify-between gap-3"><div><p className="font-bold">{candidate.sourceName}</p><p className="mt-1 text-xs text-text-muted">Captured {new Date(candidate.newCapturedAt).toLocaleString("en-IN")}</p></div><span className="stamp px-2 py-1 text-primary">Pending review</span></div>
                <p className="mt-3 text-sm leading-6 text-text-muted">{candidate.changeSummary}</p>
              </button>
            ))}
          </div>
          {selected ? <ReviewPanel candidate={selected} schemes={schemes} schemeId={schemeId} setSchemeId={setSchemeId} reason={reason} setReason={setReason} busy={busy}
            onAccept={() => { if (window.confirm("Record this evidence as applicable to the selected JanSetu scheme? This will not publish a policy.")) accept.mutate(); }}
            onReject={() => { if (window.confirm("Reject this evidence change for JanSetu policy use?")) reject.mutate(); }} /> : <State icon={<ShieldCheck className="size-5" />} text="Select a candidate to compare its evidence and record a decision." />}
        </div>
      ) : <State icon={<CheckCircle2 className="size-5" />} text="No source changes are waiting for review." />}

      <div className="mt-10 grid gap-6 lg:grid-cols-2">
        <HistoryList title="Review history" icon={<History className="size-4" />} loading={history.isLoading} error={history.isError}
          empty="No source changes have been reviewed yet." items={reviewed.map((item) => ({ id: item.id, title: item.sourceName, meta: `${item.status === "APPLICABLE" ? "Accepted" : "Rejected"}${item.schemeName ? ` · ${item.schemeName}` : ""}`, detail: item.reviewNote ?? "No reason recorded.", date: item.reviewedAt }))} />
        <HistoryList title="Append-only audit timeline" icon={<ShieldCheck className="size-4" />} loading={audit.isLoading} error={audit.isError}
          empty="No administrative source-review events have been recorded." items={(audit.data ?? []).filter((item) => item.entityType === "SOURCE_CHANGE_CANDIDATE").map((item) => ({ id: item.id, title: item.action === "SOURCE_CHANGE_ACCEPTED" ? "Evidence accepted" : "Evidence rejected", meta: item.actorEmail, detail: item.decisionReason, date: item.recordedAt }))} />
      </div>
      <AIInvestigationWorkspace candidates={history.data ?? []} schemes={schemes} />
      <PolicyPublicationWorkflow candidates={reviewed} />
    </section>
  );
}

function ReviewPanel({ candidate, schemes, schemeId, setSchemeId, reason, setReason, busy, onAccept, onReject }: { candidate: SourceChangeCandidate; schemes: SchemeResponse[]; schemeId: string; setSchemeId: (value: string) => void; reason: string; setReason: (value: string) => void; busy: boolean; onAccept: () => void; onReject: () => void }) {
  const validReason = reason.trim().length > 0;
  return <div className="rounded-lg border border-border bg-surface p-5 shadow-sm xl:sticky xl:top-4 xl:self-start">
    <div className="flex items-start justify-between gap-3"><div><p className="noticeboard-kicker text-primary">Evidence comparison</p><h3 className="mt-1 text-xl">{candidate.sourceName}</h3></div><a href={candidate.sourceUrl} target="_blank" rel="noreferrer" className="inline-flex min-h-11 items-center gap-2 rounded-lg border border-border px-3 text-sm font-bold">Official source <ExternalLink className="size-4" /></a></div>
    <div className="mt-5 grid gap-3 sm:grid-cols-2"><Evidence label="Previous snapshot" hash={candidate.previousHash} date={candidate.previousCapturedAt} excerpt={candidate.previousExcerpt} /><Evidence label="New snapshot" hash={candidate.newHash} date={candidate.newCapturedAt} excerpt={candidate.newExcerpt} /></div>
    <label className="mt-5 block text-sm font-bold">Affected scheme<select className="admin-input mt-1.5" value={schemeId} onChange={(event) => setSchemeId(event.target.value)}><option value="">Select a scheme</option>{schemes.filter((scheme) => scheme.status !== "ARCHIVED").map((scheme) => <option key={scheme.id} value={scheme.id}>{scheme.name}</option>)}</select></label>
    <label className="mt-4 block text-sm font-bold">Reviewer reason<textarea className="admin-input mt-1.5 resize-y" rows={3} required maxLength={1000} value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Explain why this change is or is not relevant to JanSetu policy records." /></label>
    <p className="mt-3 text-xs leading-5 text-text-muted">Accepting records an evidence-review decision only. Policy drafts, approvals, and publication remain separate controlled actions.</p>
    <div className="mt-5 flex flex-col gap-3 sm:flex-row"><button type="button" disabled={busy || !validReason || !schemeId} onClick={onAccept} className="inline-flex min-h-11 flex-1 items-center justify-center gap-2 rounded-lg bg-primary px-4 font-bold text-primary-foreground disabled:opacity-50"><CheckCircle2 className="size-4" />Accept as applicable</button><button type="button" disabled={busy || !validReason} onClick={onReject} className="inline-flex min-h-11 flex-1 items-center justify-center gap-2 rounded-lg border-2 border-destructive px-4 font-bold text-destructive disabled:opacity-50"><XCircle className="size-4" />Reject change</button></div>
  </div>;
}

function Evidence({ label, hash, date, excerpt }: { label: string; hash: string; date: string; excerpt: string }) { return <article className="rounded-lg border border-border bg-black/[0.02] p-4"><p className="text-xs font-bold uppercase tracking-wider text-text-muted">{label}</p><p className="mt-2 text-xs text-text-muted">{new Date(date).toLocaleString("en-IN")}</p><p className="mt-3 whitespace-pre-wrap break-words text-sm leading-6">{excerpt}</p><code className="mt-3 block break-all text-[10px] text-text-muted">{hash}</code></article>; }
function State({ icon, text, error = false }: { icon: React.ReactNode; text: string; error?: boolean }) { return <div className={`mt-6 flex min-h-32 items-center justify-center gap-3 rounded-lg border border-dashed p-6 text-sm ${error ? "border-destructive text-destructive" : "border-border text-text-muted"}`} role={error ? "alert" : "status"}>{icon}{text}</div>; }
function HistoryList({ title, icon, loading, error, empty, items }: { title: string; icon: React.ReactNode; loading: boolean; error: boolean; empty: string; items: { id: string; title: string; meta: string; detail: string; date: string | null }[] }) { return <section className="rounded-lg border border-border bg-surface p-5"><h3 className="flex items-center gap-2 text-lg font-bold">{icon}{title}</h3>{loading ? <p className="mt-4 text-sm text-text-muted">Loading history…</p> : error ? <p className="mt-4 text-sm text-destructive" role="alert">History could not be loaded.</p> : items.length ? <ol className="mt-4 divide-y divide-border">{items.map((item) => <li key={item.id} className="py-3"><div className="flex flex-wrap justify-between gap-2"><strong className="text-sm">{item.title}</strong><span className="text-xs text-text-muted">{item.date ? new Date(item.date).toLocaleString("en-IN") : ""}</span></div><p className="mt-1 text-xs font-semibold text-primary">{item.meta}</p><p className="mt-1 text-sm leading-5 text-text-muted">{item.detail}</p></li>)}</ol> : <p className="mt-4 text-sm text-text-muted">{empty}</p>}</section>; }
