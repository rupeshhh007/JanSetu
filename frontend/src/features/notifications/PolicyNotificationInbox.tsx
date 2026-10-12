"use client";

import Link from "next/link";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { BellRing, Check, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { getPolicyNotifications, markPolicyNotificationRead } from "./api";

export function PolicyNotificationInbox() {
  const client = useQueryClient();
  const inbox = useQuery({ queryKey: ["policy-notifications"], queryFn: getPolicyNotifications });
  const markRead = useMutation({
    mutationFn: markPolicyNotificationRead,
    onSuccess: () => client.invalidateQueries({ queryKey: ["policy-notifications"] }),
    onError: () => toast.error("The notification could not be marked as read."),
  });

  if (inbox.isLoading) return <div className="flex min-h-24 items-center justify-center gap-2 rounded-xl border bg-white text-sm text-muted-foreground" role="status"><Loader2 className="size-4 animate-spin" />Checking for policy updates…</div>;
  if (inbox.isError) return <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive" role="alert">Policy updates could not be loaded. <button className="font-bold underline" onClick={() => inbox.refetch()}>Try again</button></div>;
  if (!inbox.data?.length) return null;

  const unread = inbox.data.filter((item) => !item.readAt).length;
  return <section className="rounded-xl border border-primary/20 bg-accent/40 p-5 sm:p-6" aria-labelledby="policy-updates-heading">
    <div className="flex flex-wrap items-center justify-between gap-3"><div className="flex items-center gap-3"><span className="flex size-10 items-center justify-center rounded-full bg-primary text-primary-foreground"><BellRing className="size-5" /></span><div><h2 id="policy-updates-heading" className="editorial text-2xl">Policy updates</h2><p className="text-xs text-muted-foreground">{unread ? `${unread} unread eligibility update${unread === 1 ? "" : "s"}` : "All updates read"}</p></div></div></div>
    <ol className="mt-4 divide-y divide-primary/10">{inbox.data.slice(0, 5).map((item) => <li key={item.id} className={`py-4 ${item.readAt ? "opacity-70" : ""}`}><div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-start"><div><div className="flex flex-wrap items-center gap-2"><strong className="text-sm">{item.title}</strong>{!item.readAt && <span className="rounded-full bg-primary px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-primary-foreground">New</span>}</div><p className="mt-1 text-sm leading-6 text-muted-foreground">{item.message}</p><p className="mt-2 text-xs text-muted-foreground">Policy version {item.policyVersionNumber} · {new Date(item.createdAt).toLocaleString("en-IN")}</p></div><div className="flex shrink-0 gap-2">{!item.readAt && <button type="button" disabled={markRead.isPending} onClick={() => markRead.mutate(item.id)} className="inline-flex min-h-10 items-center gap-2 rounded-lg border bg-white px-3 text-xs font-bold disabled:opacity-50"><Check className="size-4" />Mark read</button>}<Link href={`/schemes/${item.schemeId}`} className="inline-flex min-h-10 items-center rounded-lg bg-primary px-3 text-xs font-bold text-primary-foreground">View scheme</Link></div></div></li>)}</ol>
  </section>;
}
