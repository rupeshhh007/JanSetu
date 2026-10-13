"use client";

import { useMemo, useState } from "react";
import axios from "axios";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  Bot,
  CheckCircle2,
  Check,
  Clock3,
  ExternalLink,
  FileSearch,
  Loader2,
  Play,
  RefreshCw,
  Send,
  ShieldCheck,
  Square,
  Wrench,
  X,
} from "lucide-react";
import { toast } from "sonner";
import type { SchemeResponse } from "@/features/schemes/types";
import {
  cancelPolicyInvestigation,
  getPolicyInvestigationEvents,
  getPolicyInvestigations,
  forwardInvestigationToPolicyDraft,
  reviewPolicyInvestigation,
  retryPolicyInvestigation,
  startPolicyInvestigation,
} from "./api";
import type {
  AgentExecutionMode,
  PolicyAgentRun,
  SourceChangeCandidate,
} from "./types";

interface Props {
  candidates: SourceChangeCandidate[];
  schemes: SchemeResponse[];
}

export function AIInvestigationWorkspace({ candidates, schemes }: Props) {
  const client = useQueryClient();
  const [candidateId, setCandidateId] = useState("");
  const [schemeId, setSchemeId] = useState("");
  const [mode, setMode] = useState<AgentExecutionMode>("DETERMINISTIC_FIXTURE");
  const [selectedRunId, setSelectedRunId] = useState<string | null>(null);
  const history = useQuery({ queryKey: ["admin-ai-investigations"], queryFn: getPolicyInvestigations });
  const resolvedCandidateId = candidateId || candidates[0]?.id || "";
  const selectedCandidate = candidates.find((candidate) => candidate.id === resolvedCandidateId) ?? null;
  const resolvedSchemeId = schemeId || selectedCandidate?.schemeId || "";
  const selectedRun = history.data?.find((run) => run.id === selectedRunId) ?? history.data?.[0] ?? null;
  const timeline = useQuery({
    queryKey: ["admin-ai-investigations", selectedRun?.id, "events"],
    queryFn: () => getPolicyInvestigationEvents(selectedRun!.id),
    enabled: Boolean(selectedRun?.id),
  });

  const refresh = async (run: PolicyAgentRun) => {
    setSelectedRunId(run.id);
    await client.invalidateQueries({ queryKey: ["admin-ai-investigations"] });
  };
  const refreshAdministration = async (run: PolicyAgentRun) => {
    await refresh(run);
    await Promise.all([
      client.invalidateQueries({ queryKey: ["admin-audit"] }),
      client.invalidateQueries({ queryKey: ["admin-policy-versions"] }),
      client.invalidateQueries({ queryKey: ["admin-source-changes"] }),
    ]);
  };
  const errorMessage = (error: unknown) => axios.isAxiosError(error) && typeof error.response?.data?.message === "string"
    ? error.response.data.message : "The investigation could not be completed safely.";
  const start = useMutation({
    mutationFn: () => startPolicyInvestigation(resolvedCandidateId, resolvedSchemeId, mode),
    onSuccess: async (run) => { await refresh(run); toast.success(run.status === "COMPLETED" ? "Investigation completed and saved for review." : "Investigation outcome recorded."); },
    onError: (error) => toast.error(errorMessage(error)),
  });
  const retry = useMutation({
    mutationFn: (run: PolicyAgentRun) => retryPolicyInvestigation(run.id, mode),
    onSuccess: async (run) => { await refresh(run); toast.success("A new investigation attempt was recorded."); },
    onError: (error) => toast.error(errorMessage(error)),
  });
  const cancel = useMutation({
    mutationFn: cancelPolicyInvestigation,
    onSuccess: async (run) => { await refresh(run); toast.success("Investigation cancelled without changing policy."); },
    onError: (error) => toast.error(errorMessage(error)),
  });
  const activeSchemes = useMemo(() => schemes.filter((scheme) => scheme.status !== "ARCHIVED"), [schemes]);
  const canStart = Boolean(resolvedCandidateId && resolvedSchemeId) && !start.isPending;

  return (
    <section className="mt-12 border-t border-border pt-8" aria-labelledby="ai-investigation-heading">
      <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-end">
        <div>
          <p className="noticeboard-kicker text-primary">04 / AI-assisted investigation</p>
          <h2 id="ai-investigation-heading" className="mt-2 text-3xl">Policy intelligence workspace</h2>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-text-muted">
            Collect source evidence and current policy for human investigation. The live model selects bounded read tools; it does not automatically infer a new policy rule. Every proposed amendment requires explicit administrator editing, validation and review.
          </p>
        </div>
        <ProcessLegend />
      </div>

      <div className="mt-6 grid gap-6 xl:grid-cols-[minmax(16rem,.62fr)_minmax(0,1.38fr)]">
        <div className="space-y-5">
          <form className="rounded-xl border border-border bg-surface p-5" onSubmit={(event) => {
            event.preventDefault();
            if (window.confirm("Start a bounded investigation? It can save a review draft but cannot publish policy.")) start.mutate();
          }}>
            <h3 className="flex items-center gap-2 text-lg font-bold"><FileSearch className="size-5 text-primary" />Start investigation</h3>
            <label className="mt-4 block text-sm font-bold">Detected change
              <select className="admin-input mt-1.5" value={resolvedCandidateId} onChange={(event) => {
                setCandidateId(event.target.value);
                const candidate = candidates.find((item) => item.id === event.target.value);
                setSchemeId(candidate?.schemeId ?? "");
              }}>
                <option value="">Select source evidence</option>
                {candidates.map((candidate) => <option key={candidate.id} value={candidate.id}>{candidate.sourceName} · {candidate.status.replace("_", " ")}</option>)}
              </select>
            </label>
            <label className="mt-4 block text-sm font-bold">Scheme scope
              <select className="admin-input mt-1.5" value={resolvedSchemeId} disabled={Boolean(selectedCandidate?.schemeId)} onChange={(event) => setSchemeId(event.target.value)}>
                <option value="">Select a scheme</option>
                {activeSchemes.map((scheme) => <option key={scheme.id} value={scheme.id}>{scheme.name}</option>)}
              </select>
            </label>
            <fieldset className="mt-4">
              <legend className="text-sm font-bold">Execution mode</legend>
              <div className="mt-2 grid grid-cols-2 gap-2">
                <ModeButton selected={mode === "DETERMINISTIC_FIXTURE"} onClick={() => setMode("DETERMINISTIC_FIXTURE")} title="Fixture" detail="Offline, repeatable" />
                <ModeButton selected={mode === "LIVE_MODEL"} onClick={() => setMode("LIVE_MODEL")} title="Live model" detail="Provider must be enabled" />
              </div>
            </fieldset>
            {selectedCandidate && <p className="mt-4 rounded-lg bg-black/[0.025] p-3 text-xs leading-5 text-text-muted">{selectedCandidate.changeSummary}</p>}
            <button type="submit" disabled={!canStart} className="mt-5 inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 font-bold text-primary-foreground disabled:opacity-50">
              {start.isPending ? <Loader2 className="size-4 animate-spin" /> : <Play className="size-4" />}{start.isPending ? "Investigating…" : "Start bounded investigation"}
            </button>
          </form>

          <InvestigationHistory runs={history.data ?? []} loading={history.isLoading} error={history.isError}
            selectedId={selectedRun?.id ?? null} onSelect={setSelectedRunId} onRetry={(run) => retry.mutate(run)} retrying={retry.isPending} />
        </div>

        <div>
          {history.isError ? <PanelState error text="Investigation history could not be loaded." action={() => history.refetch()} />
            : !selectedRun ? <PanelState text="Start an investigation or select a previous run to inspect its evidence and tool timeline." />
              : <InvestigationDetail key={selectedRun.id} run={selectedRun} events={timeline.data ?? []} eventsLoading={timeline.isLoading} eventsError={timeline.isError}
                onReload={() => timeline.refetch()} onCancel={() => cancel.mutate(selectedRun.id)} cancelling={cancel.isPending}
                onChanged={refreshAdministration} errorMessage={errorMessage} />}
        </div>
      </div>
    </section>
  );
}

function ProcessLegend() {
  return <div className="flex flex-wrap items-center gap-2 text-[11px] font-bold text-text-muted" aria-label="Controlled policy workflow">
    <span>AI suggestion</span><span aria-hidden="true">→</span><span>Deterministic validation</span><span aria-hidden="true">→</span><span>Administrator decision</span><span aria-hidden="true">→</span><span>Separate publication</span>
  </div>;
}

function ModeButton({ selected, onClick, title, detail }: { selected: boolean; onClick: () => void; title: string; detail: string }) {
  return <button type="button" aria-pressed={selected} onClick={onClick} className={`min-h-16 rounded-lg border p-3 text-left ${selected ? "border-primary bg-primary/5" : "border-border bg-white"}`}>
    <strong className="block text-sm">{title}</strong><span className="mt-0.5 block text-[11px] font-normal text-text-muted">{detail}</span>
  </button>;
}

function InvestigationHistory({ runs, loading, error, selectedId, onSelect, onRetry, retrying }: {
  runs: PolicyAgentRun[]; loading: boolean; error: boolean; selectedId: string | null;
  onSelect: (id: string) => void; onRetry: (run: PolicyAgentRun) => void; retrying: boolean;
}) {
  return <section className="rounded-xl border border-border bg-surface p-5" aria-labelledby="investigation-history-heading">
    <h3 id="investigation-history-heading" className="flex items-center gap-2 text-lg font-bold"><Clock3 className="size-5 text-primary" />Run history</h3>
    {loading ? <p className="mt-4 flex items-center gap-2 text-sm text-text-muted"><Loader2 className="size-4 animate-spin" />Loading investigations…</p>
      : error ? <p className="mt-4 text-sm text-destructive" role="alert">History is unavailable.</p>
        : runs.length ? <ol className="mt-3 divide-y divide-border">{runs.map((run) => <li key={run.id} className="py-2">
          <button type="button" onClick={() => onSelect(run.id)} aria-current={selectedId === run.id ? "true" : undefined}
            className={`w-full rounded-lg p-3 text-left ${selectedId === run.id ? "bg-primary/5" : "hover:bg-black/[0.025]"}`}>
            <span className="flex items-center justify-between gap-2"><strong className="text-sm">{run.schemeName}</strong><Status status={run.status} /></span>
            <span className="mt-1 block text-xs text-text-muted">{run.executionMode === "LIVE_MODEL" ? "Live model" : "Deterministic fixture"} · {new Date(run.startedAt).toLocaleString("en-IN")}</span>
          </button>
          {(run.status === "FAILED" || run.status === "CANCELLED") && <button type="button" disabled={retrying} onClick={() => onRetry(run)} className="ml-3 mt-1 inline-flex min-h-9 items-center gap-2 text-xs font-bold text-primary disabled:opacity-50"><RefreshCw className="size-3.5" />Retry safely</button>}
        </li>)}</ol> : <p className="mt-4 text-sm text-text-muted">No investigations have been recorded.</p>}
  </section>;
}

function InvestigationDetail({ run, events, eventsLoading, eventsError, onReload, onCancel, cancelling, onChanged, errorMessage }: {
  run: PolicyAgentRun; events: Awaited<ReturnType<typeof getPolicyInvestigationEvents>>; eventsLoading: boolean; eventsError: boolean;
  onReload: () => void; onCancel: () => void; cancelling: boolean;
  onChanged: (run: PolicyAgentRun) => Promise<void>; errorMessage: (error: unknown) => string;
}) {
  const result = run.result;
  const [reason, setReason] = useState(run.reviewReason ?? "");
  const [ruleJson, setRuleJson] = useState(run.result?.proposedPolicyRule ? JSON.stringify(run.result.proposedPolicyRule, null, 2) : "");
  const [effectiveFrom, setEffectiveFrom] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const review = useMutation({
    mutationFn: async (decision: "APPROVE" | "REJECT") => {
      if (!reason.trim()) throw new Error("Enter a reviewer reason before recording a decision.");
      let proposedPolicyRule: Record<string, unknown> | undefined;
      if (decision === "APPROVE") {
        try { proposedPolicyRule = JSON.parse(ruleJson) as Record<string, unknown>; }
        catch { throw new Error("The edited policy rule must be valid JSON."); }
      }
      if (!window.confirm(`${decision === "APPROVE" ? "Approve" : "Reject"} this investigation? The decision will be audited.`)) throw new Error("CANCELLED");
      return reviewPolicyInvestigation(run.id, decision, reason.trim(), proposedPolicyRule);
    },
    onMutate: () => setFormError(null),
    onSuccess: async (updated, decision) => {
      await onChanged(updated);
      toast.success(decision === "APPROVE" ? "Proposal approved for controlled draft creation." : "Investigation rejected and audited.");
    },
    onError: (error) => {
      if (error instanceof Error && error.message === "CANCELLED") return;
      const message = error instanceof Error && !axios.isAxiosError(error) ? error.message : errorMessage(error);
      setFormError(message); toast.error(message);
    },
  });
  const forward = useMutation({
    mutationFn: async () => {
      if (!effectiveFrom) throw new Error("Choose an effective date for the proposed draft.");
      if (!window.confirm("Create a new draft policy version from this approved proposal? This does not publish it.")) throw new Error("CANCELLED");
      return forwardInvestigationToPolicyDraft(run.id, effectiveFrom);
    },
    onMutate: () => setFormError(null),
    onSuccess: async () => {
      const updated = { ...run, reviewStatus: "FORWARDED_TO_POLICY_DRAFT" as const };
      await onChanged(updated); toast.success("A draft policy version was created. Publication still requires separate approval.");
    },
    onError: (error) => {
      if (error instanceof Error && error.message === "CANCELLED") return;
      const message = error instanceof Error && !axios.isAxiosError(error) ? error.message : errorMessage(error);
      setFormError(message); toast.error(message);
    },
  });
  return <article className="rounded-xl border border-border bg-surface p-5 sm:p-7">
    <div className="flex flex-col justify-between gap-4 border-b border-border pb-5 sm:flex-row sm:items-start">
      <div><p className="noticeboard-kicker text-primary">Investigation record</p><h3 className="mt-1 text-2xl">{run.schemeName}</h3><p className="mt-2 text-xs text-text-muted">Run {run.id}</p></div>
      <div className="flex flex-wrap items-center gap-3"><Status status={run.status} /><ReviewStatus status={run.reviewStatus} />{run.status === "RUNNING" && <button type="button" disabled={cancelling} onClick={onCancel} className="inline-flex min-h-10 items-center gap-2 rounded-lg border border-destructive px-3 text-xs font-bold text-destructive"><Square className="size-3.5" />Cancel</button>}</div>
    </div>

    <dl className="grid grid-cols-2 gap-x-5 gap-y-4 border-b border-border py-5 sm:grid-cols-4">
      <Metric label="Tool calls" value={`${run.toolCallCount} / ${run.maxToolCalls}`} />
      <Metric label="Model calls" value={`${run.modelCallCount} / ${run.maxModelCalls}`} />
      <Metric label="Estimated tokens" value={`${run.estimatedTokensUsed} / ${run.maxEstimatedTokens}`} />
      <Metric label="Mode" value={run.executionMode === "LIVE_MODEL" ? "Live model" : "Fixture"} />
    </dl>

    {run.failureMessage && <div className="mt-5 flex gap-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive" role="alert"><AlertTriangle className="mt-0.5 size-5 shrink-0" /><div><strong>Investigation stopped safely</strong><p className="mt-1">{run.failureMessage}</p><p className="mt-1 text-xs">No policy or citizen decision was changed.</p></div></div>}
    {result && <>
      <section className="mt-6" aria-labelledby="proposal-heading"><h4 id="proposal-heading" className="text-lg font-bold">Structured proposal</h4><p className="mt-2 text-sm leading-6">{result.proposedChangeSummary}</p>
        <pre className="mt-4 max-h-80 overflow-auto rounded-lg border border-border bg-black/[0.025] p-4 text-xs leading-5">{JSON.stringify(result.proposedPolicyRule, null, 2)}</pre>
      </section>
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <section><h4 className="flex items-center gap-2 text-base font-bold"><FileSearch className="size-4 text-primary" />Evidence citations</h4>{result.evidenceCitations.length ? <ol className="mt-3 space-y-3">{result.evidenceCitations.map((citation) => <li key={`${citation.snapshotId}-${citation.contentHash}`} className="rounded-lg border border-border p-4"><div className="flex justify-between gap-3"><strong className="text-sm">{citation.sourceTitle}</strong><a href={citation.sourceUrl} target="_blank" rel="noreferrer" aria-label={`Open ${citation.sourceTitle}`} className="text-primary"><ExternalLink className="size-4" /></a></div><blockquote className="mt-3 border-l-2 border-primary pl-3 text-sm leading-6">{citation.exactSpan}</blockquote><code className="mt-3 block break-all text-[10px] text-text-muted">{citation.contentHash}</code></li>)}</ol> : <p className="mt-3 text-sm text-destructive">No exact citations were produced.</p>}</section>
        <section><h4 className="flex items-center gap-2 text-base font-bold"><ShieldCheck className="size-4 text-primary" />Validation and uncertainty</h4>{result.validationIssues.length ? <ul className="mt-3 space-y-2">{result.validationIssues.map((issue, index) => <li key={`${issue.code}-${index}`} className="rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm"><strong>{issue.code}</strong><p className="mt-1 text-text-muted">{issue.message}</p></li>)}</ul> : <p className="mt-3 flex items-center gap-2 text-sm font-semibold text-primary"><CheckCircle2 className="size-4" />Typed proposal and citations passed deterministic validation.</p>}
          <ul className="mt-4 list-disc space-y-2 pl-5 text-sm leading-6 text-text-muted">{result.unverifiedClaims.map((claim) => <li key={claim}>{claim}</li>)}</ul>
          <p className="mt-4 rounded-lg bg-accent/50 p-3 text-sm font-semibold">{result.recommendedNextAction}</p>
        </section>
      </div>
      <section className="mt-7 rounded-xl border border-primary/20 bg-primary/[0.025] p-4 sm:p-5" aria-labelledby="human-review-heading">
        <h4 id="human-review-heading" className="flex items-center gap-2 text-base font-bold"><ShieldCheck className="size-4 text-primary" />Human review gate</h4>
        {run.reviewStatus === "PENDING_REVIEW" ? <>
          <p className="mt-2 text-sm leading-6 text-text-muted">Edit the typed proposal when needed, explain the decision, then approve or reject it. Approval permits draft creation only.</p>
          <label className="mt-4 block text-sm font-bold">Proposed policy rule
            <textarea value={ruleJson} onChange={(event) => setRuleJson(event.target.value)} rows={10} spellCheck={false}
              className="admin-input mt-1.5 font-mono text-xs leading-5" aria-describedby="policy-rule-help" />
          </label>
          <p id="policy-rule-help" className="mt-1 text-xs text-text-muted">The backend validates the rule type, evidence citations, candidate, and scheme before approval.</p>
          <label className="mt-4 block text-sm font-bold">Reviewer reason
            <textarea value={reason} onChange={(event) => setReason(event.target.value)} rows={3} maxLength={1000}
              className="admin-input mt-1.5" placeholder="Explain why the evidence supports this decision." />
          </label>
          <div className="mt-4 flex flex-col gap-2 sm:flex-row">
            <button type="button" disabled={review.isPending || !reason.trim()} onClick={() => review.mutate("APPROVE")} className="inline-flex min-h-11 flex-1 items-center justify-center gap-2 rounded-lg bg-primary px-4 text-sm font-bold text-primary-foreground disabled:opacity-50"><Check className="size-4" />Approve proposal</button>
            <button type="button" disabled={review.isPending || !reason.trim()} onClick={() => review.mutate("REJECT")} className="inline-flex min-h-11 flex-1 items-center justify-center gap-2 rounded-lg border border-destructive px-4 text-sm font-bold text-destructive disabled:opacity-50"><X className="size-4" />Reject investigation</button>
          </div>
        </> : <>
          <p className="mt-3 text-sm leading-6"><strong>{humanize(run.reviewStatus)}</strong>{run.reviewedAt ? ` on ${new Date(run.reviewedAt).toLocaleString("en-IN")}` : ""}</p>
          {run.reviewReason && <blockquote className="mt-3 border-l-2 border-primary pl-3 text-sm leading-6 text-text-muted">{run.reviewReason}</blockquote>}
          {run.reviewStatus === "APPROVED" && <div className="mt-5 border-t border-border pt-4">
            <label className="block text-sm font-bold">Proposed effective date
              <input type="date" value={effectiveFrom} onChange={(event) => setEffectiveFrom(event.target.value)} className="admin-input mt-1.5" />
            </label>
            <button type="button" disabled={forward.isPending || !effectiveFrom} onClick={() => forward.mutate()} className="mt-3 inline-flex min-h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 text-sm font-bold text-primary-foreground disabled:opacity-50"><Send className="size-4" />Create controlled policy draft</button>
            <p className="mt-2 text-xs text-text-muted">The new version starts as DRAFT. It is not submitted, approved, or published by this action.</p>
          </div>}
        </>}
        {formError && <p className="mt-3 rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive" role="alert">{formError}</p>}
      </section>
    </>}

    <section className="mt-7 border-t border-border pt-6" aria-labelledby="tool-timeline-heading"><div className="flex items-center justify-between gap-3"><h4 id="tool-timeline-heading" className="flex items-center gap-2 text-base font-bold"><Wrench className="size-4 text-primary" />Tool execution timeline</h4>{eventsError && <button type="button" onClick={onReload} className="text-xs font-bold text-primary">Reload</button>}</div>
      {eventsLoading ? <p className="mt-3 flex items-center gap-2 text-sm text-text-muted"><Loader2 className="size-4 animate-spin" />Loading persisted events…</p> : eventsError ? <p className="mt-3 text-sm text-destructive" role="alert">The append-only timeline could not be loaded.</p> : <ol className="mt-3 border-l border-border pl-5">{events.map((event) => <li key={event.id} className="relative pb-4"><span className={`absolute -left-[1.43rem] top-1 size-2 rounded-full ${event.outcome === "ERROR" ? "bg-destructive" : "bg-primary"}`} /><div className="flex flex-wrap items-center justify-between gap-2"><strong className="text-sm">{event.toolName ? humanize(event.toolName) : humanize(event.eventType)}</strong><span className="text-xs text-text-muted">{event.durationMs == null ? "" : `${event.durationMs} ms`}</span></div><p className="mt-0.5 text-xs text-text-muted">Step {event.sequenceNumber} · {new Date(event.recordedAt).toLocaleString("en-IN")}{event.evidenceReferences.length ? ` · ${event.evidenceReferences.length} evidence reference${event.evidenceReferences.length === 1 ? "" : "s"}` : ""}</p></li>)}</ol>}
    </section>
    <p className="mt-5 border-t border-border pt-4 text-xs leading-5 text-text-muted">This record is an AI-assisted proposal. It has not approved, published, or changed an official policy.</p>
  </article>;
}

function Status({ status }: { status: PolicyAgentRun["status"] }) {
  const style = status === "COMPLETED" ? "border-primary/30 bg-primary/5 text-primary" : status === "RUNNING" ? "border-info/30 bg-info/5 text-info" : status === "FAILED" ? "border-destructive/30 bg-destructive/5 text-destructive" : "border-warning/30 bg-warning/5 text-warning";
  return <span className={`rounded-md border px-2 py-1 text-[10px] font-bold uppercase tracking-wider ${style}`}>{status.toLowerCase()}</span>;
}
function ReviewStatus({ status }: { status: PolicyAgentRun["reviewStatus"] }) {
  const style = status === "APPROVED" || status === "FORWARDED_TO_POLICY_DRAFT" ? "border-primary/30 bg-primary/5 text-primary" : status === "REJECTED" ? "border-destructive/30 bg-destructive/5 text-destructive" : "border-warning/30 bg-warning/5 text-warning";
  return <span className={`rounded-md border px-2 py-1 text-[10px] font-bold uppercase tracking-wider ${style}`}>{humanize(status)}</span>;
}
function Metric({ label, value }: { label: string; value: string }) { return <div><dt className="text-[10px] font-bold uppercase tracking-wider text-text-muted">{label}</dt><dd className="mt-1 text-sm font-bold">{value}</dd></div>; }
function PanelState({ text, error = false, action }: { text: string; error?: boolean; action?: () => void }) { return <div className={`flex min-h-80 flex-col items-center justify-center rounded-xl border border-dashed p-8 text-center text-sm ${error ? "border-destructive text-destructive" : "border-border text-text-muted"}`} role={error ? "alert" : "status"}><Bot className="mb-4 size-8" /><p>{text}</p>{action && <button type="button" onClick={action} className="mt-3 font-bold underline">Try again</button>}</div>; }
function humanize(value: string) { return value.toLowerCase().replaceAll("_", " ").replace(/^./, (letter) => letter.toUpperCase()); }
