"use client";

import { useState } from "react";
import axios from "axios";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowRight, CheckCircle2, FileClock, Loader2, Send, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { createPolicyDraft, getPolicyVersions, transitionPolicy } from "./api";
import type { PolicyVersion, SourceChangeCandidate } from "./types";

const FACTS = [
  { value: "IS_STUDENT", label: "Citizen is a student" },
  { value: "IS_FARMER", label: "Citizen is a farmer" },
  { value: "HAS_DISABILITY", label: "Citizen has a disability" },
] as const;

export function PolicyPublicationWorkflow({ candidates }: { candidates: SourceChangeCandidate[] }) {
  const eligible = candidates.filter((candidate) => candidate.status === "APPLICABLE" && candidate.schemeId);
  const [candidateId, setCandidateId] = useState(eligible[0]?.id ?? "");
  const effectiveCandidateId = candidateId || eligible[0]?.id || "";
  const selected = eligible.find((candidate) => candidate.id === effectiveCandidateId) ?? null;
  const client = useQueryClient();
  const versions = useQuery({
    queryKey: ["admin-policy-versions", selected?.schemeId],
    queryFn: () => getPolicyVersions(selected!.schemeId!),
    enabled: Boolean(selected?.schemeId),
  });
  const linked = versions.data?.find((version) => version.id === selected?.proposedPolicyVersionId);
  const [effectiveFrom, setEffectiveFrom] = useState("");
  const [fact, setFact] = useState<(typeof FACTS)[number]["value"]>("IS_STUDENT");
  const [expectedValue, setExpectedValue] = useState(true);
  const [label, setLabel] = useState("");
  const [reason, setReason] = useState("");

  const refresh = async () => {
    await Promise.all([
      client.invalidateQueries({ queryKey: ["admin-source-changes"] }),
      client.invalidateQueries({ queryKey: ["admin-policy-versions"] }),
      client.invalidateQueries({ queryKey: ["admin-audit"] }),
    ]);
  };
  const handleError = (error: unknown) => {
    const message = axios.isAxiosError(error) && typeof error.response?.data?.message === "string"
      ? error.response.data.message : "The policy action could not be completed.";
    toast.error(message);
    if (axios.isAxiosError(error) && error.response?.status === 409) void refresh();
  };
  const draft = useMutation({
    mutationFn: () => createPolicyDraft(selected!.id, {
      effectiveFrom,
      policyRule: { type: "booleanMatch", id: `source-${selected!.id}`, label: label.trim(), fact, expectedValue },
    }),
    onSuccess: async () => { toast.success("A source-linked draft was created. It is not published."); await refresh(); },
    onError: handleError,
  });
  const transition = useMutation({
    mutationFn: ({ version, action }: { version: PolicyVersion; action: "submit-review" | "approve" | "publish" }) =>
      transitionPolicy(version.schemeId, version.id, action, reason.trim()),
    onSuccess: async (version) => {
      toast.success(version.status === "PUBLISHED" ? "Policy published; affected citizens were reassessed." : `Policy moved to ${version.status.toLowerCase().replace("_", " ")}.`);
      setReason("");
      await refresh();
    },
    onError: handleError,
  });

  const nextAction = linked?.status === "DRAFT" ? "submit-review" : linked?.status === "IN_REVIEW" ? "approve" : linked?.status === "APPROVED" ? "publish" : null;
  const nextLabel = nextAction === "submit-review" ? "Submit for review" : nextAction === "approve" ? "Approve policy" : "Publish policy";
  const busy = draft.isPending || transition.isPending;

  return <section className="mt-10 rounded-xl border border-border bg-surface p-5 sm:p-7" aria-labelledby="policy-workflow-heading">
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div><p className="noticeboard-kicker text-primary">04 / Controlled publication</p><h2 id="policy-workflow-heading" className="mt-2 text-3xl">Evidence to policy</h2><p className="mt-2 max-w-2xl text-sm leading-6 text-text-muted">Turn accepted evidence into a draft, review it, then publish it as an explicit final action. Every transition requires a reason and creates audit evidence.</p></div>
      <ShieldCheck className="size-8 text-primary" aria-hidden="true" />
    </div>
    {!eligible.length ? <WorkflowState text="Accept a policy-relevant source change before creating a policy draft." /> : <div className="mt-6 grid gap-6 lg:grid-cols-[minmax(16rem,.72fr)_minmax(0,1.4fr)]">
      <div><label className="text-sm font-bold" htmlFor="policy-candidate">Accepted evidence</label><select id="policy-candidate" className="admin-input mt-2" value={effectiveCandidateId} onChange={(event) => setCandidateId(event.target.value)}>{eligible.map((candidate) => <option key={candidate.id} value={candidate.id}>{candidate.schemeName} · {candidate.sourceName}</option>)}</select>
        {selected && <div className="mt-4 rounded-lg border border-border bg-black/[0.02] p-4"><p className="text-xs font-bold uppercase tracking-wider text-primary">Evidence summary</p><p className="mt-2 text-sm leading-6">{selected.changeSummary}</p><p className="mt-3 text-xs text-text-muted">Accepted {selected.reviewedAt ? new Date(selected.reviewedAt).toLocaleString("en-IN") : ""} by {selected.reviewedBy}</p></div>}
      </div>
      {versions.isLoading ? <WorkflowState text="Loading policy history…" loading /> : versions.isError ? <WorkflowState text="Policy history could not be loaded." /> : linked ? <div>
        <div className="flex flex-wrap items-center justify-between gap-3"><div><p className="text-sm font-bold">Policy version {linked.versionNumber}</p><p className="mt-1 text-xs text-text-muted">Effective {new Date(`${linked.effectiveFrom}T00:00:00`).toLocaleDateString("en-IN")} · revision {linked.revision}</p></div><span className="stamp px-3 py-1 text-primary">{linked.status.replace("_", " ")}</span></div>
        <div className="mt-4 rounded-lg border border-border p-4"><p className="text-xs font-bold uppercase tracking-wider text-text-muted">Recorded rule</p><pre className="mt-2 overflow-x-auto whitespace-pre-wrap text-xs leading-5">{JSON.stringify(linked.policyRule, null, 2)}</pre></div>
        {nextAction ? <div className="mt-5"><label className="text-sm font-bold" htmlFor="transition-reason">Administrative reason</label><textarea id="transition-reason" className="admin-input mt-2 resize-y" rows={3} maxLength={1000} value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Record why this lifecycle transition is justified." /><button type="button" className="mt-3 inline-flex min-h-11 items-center gap-2 rounded-lg bg-primary px-5 font-bold text-primary-foreground disabled:opacity-50" disabled={busy || !reason.trim()} onClick={() => { if (window.confirm(`${nextLabel}? This action will be recorded in the append-only audit trail.`)) transition.mutate({ version: linked, action: nextAction }); }}>{nextAction === "publish" ? <CheckCircle2 className="size-4" /> : <Send className="size-4" />}{busy ? "Recording…" : nextLabel}</button>{nextAction === "publish" && <p className="mt-3 text-xs leading-5 text-text-muted">Publication makes this version active, reassesses citizen profiles, and creates private inbox notifications.</p>}</div> : <p className="mt-5 flex items-center gap-2 text-sm font-semibold text-primary"><CheckCircle2 className="size-4" />This policy version has completed its controlled lifecycle.</p>}
      </div> : <form onSubmit={(event) => { event.preventDefault(); if (window.confirm("Create a policy draft from this accepted evidence? No policy will be published.")) draft.mutate(); }}>
        <div className="grid gap-4 sm:grid-cols-2"><label className="text-sm font-bold">Effective date<input required type="date" className="admin-input mt-2" value={effectiveFrom} onChange={(event) => setEffectiveFrom(event.target.value)} /></label><label className="text-sm font-bold">Citizen fact<select className="admin-input mt-2" value={fact} onChange={(event) => setFact(event.target.value as typeof fact)}>{FACTS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label></div>
        <label className="mt-4 block text-sm font-bold">Rule description<input required maxLength={200} className="admin-input mt-2" value={label} onChange={(event) => setLabel(event.target.value)} placeholder="Example: Applicant must be enrolled as a student" /></label>
        <label className="mt-4 flex min-h-11 items-center gap-3 text-sm font-bold"><input type="checkbox" checked={expectedValue} onChange={(event) => setExpectedValue(event.target.checked)} />Fact must be true</label>
        <button disabled={busy || !effectiveFrom || !label.trim()} className="mt-4 inline-flex min-h-11 items-center gap-2 rounded-lg bg-primary px-5 font-bold text-primary-foreground disabled:opacity-50"><FileClock className="size-4" />{draft.isPending ? "Creating…" : "Create source-linked draft"}</button>
      </form>}
    </div>}
  </section>;
}

function WorkflowState({ text, loading = false }: { text: string; loading?: boolean }) {
  return <div className="mt-6 flex min-h-28 items-center justify-center gap-3 rounded-lg border border-dashed border-border p-5 text-sm text-text-muted">{loading ? <Loader2 className="size-4 animate-spin" /> : <ArrowRight className="size-4" />}{text}</div>;
}
