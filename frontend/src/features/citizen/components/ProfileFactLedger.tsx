"use client";

import { CheckCircle2, Clock3, LockKeyhole, ShieldCheck, UsersRound } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useProfileFacts } from "../hooks/useProfileFacts";
import { useUpdateProfileFact } from "../hooks/useUpdateProfileFact";
import type { ProfileFactKey, ProfileFactResponse } from "../types";

const FACT_LABELS: Record<ProfileFactKey, string> = {
  BIRTH_DATE: "Date of birth",
  ANNUAL_FAMILY_INCOME: "Annual family income",
  CATEGORY: "Social category",
  PRIMARY_OCCUPATION: "Primary occupation",
  STATE: "State of residence",
  IS_STUDENT: "Student status",
  IS_FARMER: "Farmer status",
  HAS_DISABILITY: "Disability status",
};

const VERIFICATION_LABELS: Record<ProfileFactResponse["verificationStatus"], string> = {
  SELF_DECLARED: "Self-declared",
  DOCUMENT_VERIFIED: "Document verified",
  AUTHORITATIVE: "Official record",
};

function displayValue(fact: ProfileFactResponse) {
  if (fact.factKey === "IS_STUDENT" || fact.factKey === "IS_FARMER" || fact.factKey === "HAS_DISABILITY") {
    return fact.value === "true" ? "Yes" : "No";
  }
  if (fact.factKey === "ANNUAL_FAMILY_INCOME") {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 0,
    }).format(Number(fact.value));
  }
  return fact.value.replaceAll("_", " ");
}

export function ProfileFactLedger({ enabled }: { enabled: boolean }) {
  const facts = useProfileFacts(enabled);
  const updateFact = useUpdateProfileFact();

  if (!enabled) return null;

  return (
    <Card>
      <CardHeader className="border-b">
        <div className="flex items-start gap-3">
          <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <ShieldCheck className="size-5" aria-hidden="true" />
          </span>
          <div>
            <p className="noticeboard-kicker text-primary">Privacy controls</p>
            <CardTitle className="mt-1 text-xl">Your profile fact ledger</CardTitle>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
              Each fact used in recorded policy checks has its own source, verification, and sharing setting. Facts stay private unless you explicitly allow assisted-support reuse.
            </p>
          </div>
        </div>
      </CardHeader>
      <CardContent className="pt-5">
        {facts.isLoading ? (
          <p className="text-sm text-muted-foreground" role="status">Loading your privacy controls…</p>
        ) : facts.isError ? (
          <p className="text-sm text-destructive" role="alert">We could not load your profile facts. Refresh to try again.</p>
        ) : !facts.data?.length ? (
          <div className="rounded-lg border border-dashed bg-muted/30 p-5 text-sm leading-6 text-muted-foreground">
            Save your profile once to create private fact records. You can then choose exactly what to reuse for assisted support.
          </div>
        ) : (
          <ul className="divide-y rounded-lg border">
            {facts.data.map((fact) => {
              const reusable = fact.consentScope === "CASEWORKER_REUSE";
              const updating = updateFact.isPending && updateFact.variables?.factKey === fact.factKey;
              return (
                <li key={fact.factKey} className="p-4 sm:flex sm:items-center sm:justify-between sm:gap-6">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="font-medium">{FACT_LABELS[fact.factKey]}</h3>
                      <Badge variant={fact.active ? "secondary" : "outline"}>
                        {fact.active ? "Active" : "Expired"}
                      </Badge>
                      <Badge variant="outline">{VERIFICATION_LABELS[fact.verificationStatus]}</Badge>
                    </div>
                    <p className="mt-1 text-sm text-muted-foreground">{displayValue(fact)}</p>
                    {fact.expiresAt ? (
                      <p className="mt-2 inline-flex items-center gap-1.5 text-xs text-muted-foreground">
                        <Clock3 className="size-3.5" aria-hidden="true" />
                        Expires {new Date(fact.expiresAt).toLocaleDateString("en-IN")}
                      </p>
                    ) : null}
                  </div>
                  <label className="mt-4 flex cursor-pointer items-center gap-3 text-sm sm:mt-0 sm:max-w-56">
                    <input
                      type="checkbox"
                      className="size-4 shrink-0 accent-primary"
                      checked={reusable}
                      disabled={!fact.active || updating}
                      onChange={(event) =>
                        updateFact.mutate({
                          factKey: fact.factKey,
                          data: {
                            value: fact.value,
                            consentToCaseworkerReuse: event.target.checked,
                            expiresAt: fact.expiresAt,
                          },
                        })
                      }
                    />
                    <span className="flex items-center gap-1.5 leading-5">
                      {reusable ? <UsersRound className="size-4 text-primary" aria-hidden="true" /> : <LockKeyhole className="size-4 text-muted-foreground" aria-hidden="true" />}
                      {reusable ? "Allow assisted-support reuse" : "Keep private"}
                    </span>
                  </label>
                </li>
              );
            })}
          </ul>
        )}
        <p className="mt-4 flex gap-2 text-xs leading-5 text-muted-foreground">
          <CheckCircle2 className="mt-0.5 size-3.5 shrink-0 text-primary" aria-hidden="true" />
          Your choice affects reuse only. It does not change your recorded value or a scheme’s official decision.
        </p>
      </CardContent>
    </Card>
  );
}
