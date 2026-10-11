"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  getAdminSchemes,
  createScheme,
  deleteScheme,
  publishScheme,
} from "@/features/schemes/api/schemesApi";
import { Trash2, CheckCircle2, AlertCircle } from "lucide-react";
import { toast } from "sonner";
import { useAuthStore } from "@/store/useAuthStore";
import {
  getAdminApplications,
  updateApplicationStatus,
} from "@/features/applications/api/applicationsApi";
import type { ApplicationStatus } from "@/features/applications/types";
import { ApplicationStatusBadge } from "@/features/applications/components/ApplicationStatusBadge";
import type {
  Category,
  EligibilityCriteria,
  PrimaryOccupation,
  SchemeCreateRequest,
} from "@/features/schemes/types";

const CATEGORIES: Category[] = ["GENERAL", "OBC", "SC", "ST"];
const OCCUPATIONS: PrimaryOccupation[] = [
  "SALARIED",
  "BUSINESS",
  "DAILY_WAGE",
  "UNEMPLOYED",
  "STUDENT",
  "FARMER",
];

const emptyForm = {
  name: "",
  description: "",
  state: "",
  department: "",
  officialUrl: "",
  sourceName: "",
  lastVerifiedAt: "",
  benefits: "",
  requiredDocuments: "",
  minAge: "",
  maxAge: "",
  maxFamilyIncome: "",
  requiredCategories: [] as Category[],
  allowedOccupations: [] as PrimaryOccupation[],
  requiresStudent: false,
  requiresFarmer: false,
  requiresDisability: false,
};

function splitLines(value: string) {
  return value
    .split("\n")
    .map((item) => item.trim())
    .filter(Boolean);
}

export default function AdminDashboardPage() {
  const role = useAuthStore((state) => state.role);
  const queryClient = useQueryClient();
  const { data: schemes, isLoading } = useQuery({
    queryKey: ["admin-schemes"],
    queryFn: getAdminSchemes,
    enabled: role === "ADMIN",
  });
  const { data: applications, isLoading: applicationsLoading } = useQuery({
    queryKey: ["admin-applications"],
    queryFn: getAdminApplications,
    enabled: role === "ADMIN",
  });

  const deleteMutation = useMutation({
    mutationFn: deleteScheme,
    onSuccess: () => {
      toast.success("Scheme archived successfully.");
      queryClient.invalidateQueries({ queryKey: ["schemes"] });
      queryClient.invalidateQueries({ queryKey: ["admin-schemes"] });
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      queryClient.invalidateQueries({ queryKey: ["eligibility"] });
    },
  });

  const createMutation = useMutation({
    mutationFn: createScheme,
    onSuccess: () => {
      toast.success(
        "Draft saved. Publish it when the source details are ready.",
      );
      queryClient.invalidateQueries({ queryKey: ["schemes"] });
      queryClient.invalidateQueries({ queryKey: ["admin-schemes"] });
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      queryClient.invalidateQueries({ queryKey: ["eligibility"] });
      setFormData(emptyForm);
    },
    onError: () =>
      toast.error(
        "Could not save this draft. Check the required fields and try again.",
      ),
  });

  const publishMutation = useMutation({
    mutationFn: publishScheme,
    onSuccess: () => {
      toast.success("Scheme published to the public catalogue.");
      queryClient.invalidateQueries({ queryKey: ["schemes"] });
      queryClient.invalidateQueries({ queryKey: ["admin-schemes"] });
      queryClient.invalidateQueries({ queryKey: ["recommendations"] });
      queryClient.invalidateQueries({ queryKey: ["eligibility"] });
    },
    onError: () =>
      toast.error(
        "Could not publish this scheme. Complete the required details first.",
      ),
  });

  const statusMutation = useMutation({
    mutationFn: updateApplicationStatus,
    onSuccess: () => {
      toast.success("Application status updated.");
      queryClient.invalidateQueries({ queryKey: ["applications"] });
      queryClient.invalidateQueries({ queryKey: ["admin-applications"] });
    },
    onError: () => toast.error("Could not update this application status."),
  });

  const [formData, setFormData] = useState({
    ...emptyForm,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const eligibilityCriteria: EligibilityCriteria = {
      minAge: formData.minAge ? Number(formData.minAge) : null,
      maxAge: formData.maxAge ? Number(formData.maxAge) : null,
      maxFamilyIncome: formData.maxFamilyIncome
        ? Number(formData.maxFamilyIncome)
        : null,
      requiredCategories: formData.requiredCategories.length
        ? formData.requiredCategories
        : null,
      allowedOccupations: formData.allowedOccupations.length
        ? formData.allowedOccupations
        : null,
      requiresStudent: formData.requiresStudent || null,
      requiresFarmer: formData.requiresFarmer || null,
      requiresDisability: formData.requiresDisability || null,
    };
    const request: SchemeCreateRequest = {
      name: formData.name,
      description: formData.description,
      state: formData.state,
      department: formData.department,
      officialUrl: formData.officialUrl || undefined,
      sourceName: formData.sourceName || undefined,
      lastVerifiedAt: formData.lastVerifiedAt
        ? new Date(`${formData.lastVerifiedAt}T00:00:00Z`).toISOString()
        : undefined,
      benefits: splitLines(formData.benefits),
      requiredDocuments: splitLines(formData.requiredDocuments),
      eligibilityCriteria,
    };
    createMutation.mutate(request);
  };

  const handleStatusUpdate = (
    applicationId: string,
    status: ApplicationStatus,
  ) => {
    const remarks = window.prompt("Add a review remark for the audit trail:");
    if (!remarks?.trim()) return;
    statusMutation.mutate({ applicationId, status, remarks: remarks.trim() });
  };

  if (role !== "ADMIN") {
    return (
      <div className="paper-card mx-auto flex min-h-80 max-w-xl flex-col items-center justify-center p-8 text-center">
        <AlertCircle className="size-12 text-destructive" aria-hidden="true" />
        <p className="noticeboard-kicker mt-5 text-destructive">
          Restricted desk
        </p>
        <h1 className="mt-2 text-3xl">Access denied.</h1>
        <p className="mt-3 text-text-muted">
          This workspace is only available to authorised scheme administrators.
        </p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-8 border-b border-border py-5">
        <p className="noticeboard-kicker text-primary">
          Administrator workspace / data stewardship
        </p>
        <h1 className="mt-2 text-4xl leading-none sm:text-5xl">Scheme desk</h1>
        <p className="mt-3 max-w-2xl text-text-muted">
          Publish source-linked schemes and review tracked applications.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        {/* CREATE FORM */}
        <div className="lg:col-span-5">
          <div className="paper-card p-5 sm:p-7">
            <p className="noticeboard-kicker text-primary">01 / New record</p>
            <h2 className="mt-2 text-2xl">Add a scheme</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              <div>
                <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">
                  Scheme Name
                </label>
                <input
                  required
                  type="text"
                  className="w-full rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                />
              </div>
              <div>
                <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">
                  Description
                </label>
                <textarea
                  required
                  rows={3}
                  className="w-full resize-none rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">
                    State
                  </label>
                  <input
                    required
                    type="text"
                    placeholder="e.g. Maharashtra"
                    className="w-full rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                    value={formData.state}
                    onChange={(e) =>
                      setFormData({ ...formData, state: e.target.value })
                    }
                  />
                </div>
                <div>
                  <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">
                    Department
                  </label>
                  <input
                    required
                    type="text"
                    className="w-full rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                    value={formData.department}
                    onChange={(e) =>
                      setFormData({ ...formData, department: e.target.value })
                    }
                  />
                </div>
              </div>
              <div className="grid gap-4 border-b border-border py-5 sm:grid-cols-2">
                <div className="sm:col-span-2">
                  <p className="noticeboard-kicker text-primary">
                    Source and verification
                  </p>
                </div>
                <Field label="Official scheme URL">
                  <input
                    type="url"
                    placeholder="https://…"
                    className="admin-input"
                    value={formData.officialUrl}
                    onChange={(e) =>
                      setFormData({ ...formData, officialUrl: e.target.value })
                    }
                  />
                </Field>
                <Field label="Source name">
                  <input
                    placeholder="Department / portal"
                    className="admin-input"
                    value={formData.sourceName}
                    onChange={(e) =>
                      setFormData({ ...formData, sourceName: e.target.value })
                    }
                  />
                </Field>
                <Field label="Last verified">
                  <input
                    type="date"
                    className="admin-input"
                    value={formData.lastVerifiedAt}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        lastVerifiedAt: e.target.value,
                      })
                    }
                  />
                </Field>
                <div />
                <Field label="Benefits (one per line)">
                  <textarea
                    rows={3}
                    className="admin-input resize-y"
                    placeholder="Cash support\nHealth cover"
                    value={formData.benefits}
                    onChange={(e) =>
                      setFormData({ ...formData, benefits: e.target.value })
                    }
                  />
                </Field>
                <Field label="Required documents (one per line)">
                  <textarea
                    rows={3}
                    className="admin-input resize-y"
                    placeholder="Identity proof\nIncome certificate"
                    value={formData.requiredDocuments}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        requiredDocuments: e.target.value,
                      })
                    }
                  />
                </Field>
              </div>
              <fieldset className="space-y-4 border-b border-border pb-5">
                <legend className="noticeboard-kicker text-primary">
                  Eligibility builder
                </legend>
                <p className="text-xs text-text-muted">
                  Leave a field blank when it is not a requirement. JanSetu
                  records the eligibility requirements for you.
                </p>
                <div className="grid gap-3 sm:grid-cols-3">
                  <Field label="Minimum age">
                    <input
                      min="0"
                      type="number"
                      className="admin-input"
                      value={formData.minAge}
                      onChange={(e) =>
                        setFormData({ ...formData, minAge: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Maximum age">
                    <input
                      min="0"
                      type="number"
                      className="admin-input"
                      value={formData.maxAge}
                      onChange={(e) =>
                        setFormData({ ...formData, maxAge: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Maximum family income">
                    <input
                      min="0"
                      type="number"
                      className="admin-input"
                      value={formData.maxFamilyIncome}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          maxFamilyIncome: e.target.value,
                        })
                      }
                    />
                  </Field>
                </div>
                <ChoiceGroup
                  label="Eligible categories"
                  values={CATEGORIES}
                  selected={formData.requiredCategories}
                  onChange={(value) =>
                    setFormData({
                      ...formData,
                      requiredCategories: toggle(
                        formData.requiredCategories,
                        value,
                      ),
                    })
                  }
                />
                <ChoiceGroup
                  label="Eligible occupations"
                  values={OCCUPATIONS}
                  selected={formData.allowedOccupations}
                  onChange={(value) =>
                    setFormData({
                      ...formData,
                      allowedOccupations: toggle(
                        formData.allowedOccupations,
                        value,
                      ),
                    })
                  }
                />
                <div className="flex flex-wrap gap-3">
                  <Check
                    label="Students only"
                    checked={formData.requiresStudent}
                    onChange={(requiresStudent) =>
                      setFormData({ ...formData, requiresStudent })
                    }
                  />
                  <Check
                    label="Farmers only"
                    checked={formData.requiresFarmer}
                    onChange={(requiresFarmer) =>
                      setFormData({ ...formData, requiresFarmer })
                    }
                  />
                  <Check
                    label="Persons with disability only"
                    checked={formData.requiresDisability}
                    onChange={(requiresDisability) =>
                      setFormData({ ...formData, requiresDisability })
                    }
                  />
                </div>
              </fieldset>
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="mt-4 flex min-h-12 w-full items-center justify-center border border-border rounded-lg bg-primary px-4 py-3.5 text-[15px] font-bold text-primary-foreground shadow-sm transition-transform hover:translate-x-px hover:translate-y-px hover:shadow-none disabled:opacity-50"
              >
                {createMutation.isPending ? "Saving…" : "Save draft"}
              </button>
            </form>
          </div>
        </div>

        {/* LIST SCHEMES */}
        <div className="lg:col-span-7 flex flex-col gap-4">
          <h2 className="noticeboard-kicker text-primary">
            02 / Published scheme records
          </h2>
          {isLoading ? (
            <div className="h-40 flex items-center justify-center">
              <span className="text-muted-foreground font-medium animate-pulse">
                Loading schemes...
              </span>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {schemes?.map((scheme) => (
                <div
                  key={scheme.id}
                  className="group flex items-start justify-between gap-4 border border-border rounded-lg bg-surface p-5 shadow-sm transition-all hover:translate-x-px hover:translate-y-px hover:shadow-[2px_2px_0_#151515]"
                >
                  <div className="flex-1 min-w-0">
                    <h3 className="text-[17px] font-bold text-foreground mb-1 truncate">
                      {scheme.name}
                    </h3>
                    <p className="text-[14px] text-muted-foreground line-clamp-2 leading-relaxed">
                      {scheme.description}
                    </p>
                    <div className="mt-4 flex flex-wrap gap-2">
                      <span className="stamp px-2 py-1 text-foreground/80">
                        {scheme.state}
                      </span>
                      <span className="stamp px-2 py-1 text-foreground/80">
                        {scheme.department}
                      </span>
                      <span
                        className={`stamp px-2 py-1 ${scheme.status === "PUBLISHED" ? "text-success" : scheme.status === "ARCHIVED" ? "text-text-muted" : "text-primary"}`}
                      >
                        {scheme.status}
                      </span>
                    </div>
                    <div className="mt-3 space-y-1 text-xs text-text-muted">
                      <p>
                        Source: {scheme.sourceName || "Not recorded"}
                        {scheme.lastVerifiedAt
                          ? ` · verified ${new Date(scheme.lastVerifiedAt).toLocaleDateString("en-IN")}`
                          : ""}
                      </p>
                      {scheme.benefits.length > 0 && (
                        <p>
                          Benefits: {scheme.benefits.slice(0, 2).join(" · ")}
                        </p>
                      )}
                      {scheme.requiredDocuments.length > 0 && (
                        <p>
                          Documents:{" "}
                          {scheme.requiredDocuments.slice(0, 2).join(" · ")}
                        </p>
                      )}
                    </div>
                  </div>
                  <div className="flex shrink-0 flex-col gap-2">
                    {scheme.status === "DRAFT" && (
                      <button
                        onClick={() => publishMutation.mutate(scheme.id)}
                        disabled={publishMutation.isPending}
                        className="min-h-11 border border-border rounded-lg bg-accent px-3 text-xs font-bold"
                      >
                        Publish
                      </button>
                    )}
                    {scheme.status !== "ARCHIVED" && (
                      <button
                        onClick={() => {
                          if (
                            confirm(
                              "Archive this scheme? It will be removed from the public catalogue but retained for audit history.",
                            )
                          ) {
                            deleteMutation.mutate(scheme.id);
                          }
                        }}
                        className="flex size-11 shrink-0 items-center justify-center border-2 border-destructive bg-surface text-destructive transition-all hover:bg-destructive hover:text-white"
                        aria-label={`Archive ${scheme.name}`}
                      >
                        <Trash2 className="size-4.5" />
                      </button>
                    )}
                  </div>
                </div>
              ))}
              {schemes?.length === 0 && (
                <div className="border-2 border-dashed border-foreground bg-surface p-12 text-center">
                  <CheckCircle2 className="mx-auto size-12 text-muted-foreground/30 mb-3" />
                  <h3 className="text-lg font-bold text-foreground/70">
                    No schemes found
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Add one using the form to get started.
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      <section className="mt-12 border-t border-border pt-8">
        <p className="noticeboard-kicker text-primary">
          03 / Case review queue
        </p>
        <h2 className="mt-2 text-3xl">Citizen applications</h2>
        {applicationsLoading ? (
          <p className="mt-5 text-text-muted">Loading application queue...</p>
        ) : applications?.length ? (
          <div className="mt-5 grid gap-4 md:grid-cols-2">
            {applications.map((application) => (
              <article
                key={application.applicationId}
                className="border border-border rounded-lg bg-surface p-5 shadow-sm"
              >
                <div className="flex items-start justify-between gap-3">
                  <h3 className="font-bold">{application.schemeName}</h3>
                  <ApplicationStatusBadge status={application.status} />
                </div>
                <p className="mt-2 text-xs text-text-muted">
                  Submitted {new Date(application.submittedAt).toLocaleString()}
                </p>
                <div className="mt-4 flex flex-wrap gap-2">
                  {application.status === "SUBMITTED" && (
                    <>
                      <button
                        className="min-h-11 border border-border rounded-lg bg-accent px-3 font-bold"
                        onClick={() =>
                          handleStatusUpdate(
                            application.applicationId,
                            "UNDER_REVIEW",
                          )
                        }
                      >
                        Start review
                      </button>
                      <button
                        className="min-h-11 border-2 border-destructive px-3 font-bold text-destructive"
                        onClick={() =>
                          handleStatusUpdate(
                            application.applicationId,
                            "REJECTED",
                          )
                        }
                      >
                        Reject
                      </button>
                    </>
                  )}
                  {application.status === "UNDER_REVIEW" && (
                    <>
                      <button
                        className="min-h-11 border border-border rounded-lg bg-success px-3 font-bold"
                        onClick={() =>
                          handleStatusUpdate(
                            application.applicationId,
                            "APPROVED",
                          )
                        }
                      >
                        Approve
                      </button>
                      <button
                        className="min-h-11 border-2 border-destructive px-3 font-bold text-destructive"
                        onClick={() =>
                          handleStatusUpdate(
                            application.applicationId,
                            "REJECTED",
                          )
                        }
                      >
                        Reject
                      </button>
                    </>
                  )}
                  {!["SUBMITTED", "UNDER_REVIEW"].includes(
                    application.status,
                  ) && (
                    <span className="text-sm text-text-muted">
                      This case is in a terminal state.
                    </span>
                  )}
                </div>
              </article>
            ))}
          </div>
        ) : (
          <p className="mt-5 border-2 border-dashed border-foreground p-6 text-text-muted">
            No citizen applications are waiting.
          </p>
        )}
      </section>
    </div>
  );
}

function Field({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block text-sm font-bold text-foreground">
      <span className="mb-1.5 block">{label}</span>
      {children}
    </label>
  );
}

function toggle<T extends string>(values: T[], value: T) {
  return values.includes(value)
    ? values.filter((item) => item !== value)
    : [...values, value];
}

function ChoiceGroup<T extends string>({
  label,
  values,
  selected,
  onChange,
}: {
  label: string;
  values: readonly T[];
  selected: T[];
  onChange: (value: T) => void;
}) {
  return (
    <fieldset>
      <legend className="mb-2 text-sm font-bold">{label}</legend>
      <div className="flex flex-wrap gap-2">
        {values.map((value) => (
          <label
            key={value}
            className="flex min-h-10 items-center gap-2 border border-border rounded-lg bg-surface px-3 text-xs font-bold"
          >
            <input
              type="checkbox"
              checked={selected.includes(value)}
              onChange={() => onChange(value)}
            />
            {value.replaceAll("_", " ")}
          </label>
        ))}
      </div>
    </fieldset>
  );
}

function Check({
  label,
  checked,
  onChange,
}: {
  label: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
}) {
  return (
    <label className="flex min-h-10 items-center gap-2 border border-border rounded-lg bg-surface px-3 text-xs font-bold">
      <input
        type="checkbox"
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
      {label}
    </label>
  );
}
