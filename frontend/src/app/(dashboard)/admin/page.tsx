"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getSchemes, createScheme, deleteScheme } from "@/features/schemes/api/schemesApi";
import { PlusCircle, Trash2, CheckCircle2, AlertCircle } from "lucide-react";
import { toast } from "sonner";
import { useAuthStore } from "@/store/useAuthStore";
import { useRouter } from "next/navigation";

export default function AdminDashboardPage() {
  const role = useAuthStore((state) => state.role);
  const router = useRouter();

  if (role !== "ADMIN") {
    // If not admin, we can render a simple unauthorized message (or it would be blocked by a route guard ideally)
    return (
      <div className="flex h-[80vh] flex-col items-center justify-center p-6 text-center">
        <AlertCircle className="size-12 text-destructive mb-4" />
        <h1 className="text-2xl font-bold text-foreground">Access Denied</h1>
        <p className="mt-2 text-muted-foreground">You do not have permission to view the Admin Panel.</p>
      </div>
    );
  }

  const queryClient = useQueryClient();
  const { data: schemes, isLoading } = useQuery({
    queryKey: ["schemes"],
    queryFn: getSchemes,
  });

  const deleteMutation = useMutation({
    mutationFn: deleteScheme,
    onSuccess: () => {
      toast.success("Scheme deleted successfully.");
      queryClient.invalidateQueries({ queryKey: ["schemes"] });
    },
  });

  const createMutation = useMutation({
    mutationFn: createScheme,
    onSuccess: () => {
      toast.success("Scheme created successfully.");
      queryClient.invalidateQueries({ queryKey: ["schemes"] });
      setFormData({ name: "", description: "", state: "", department: "", eligibilityCriteria: "{}" });
    },
    onError: () => toast.error("Failed to create scheme. Check your JSON format."),
  });

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    state: "",
    department: "",
    eligibilityCriteria: "{\n  \"maxIncome\": 500000\n}",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const criteriaObj = JSON.parse(formData.eligibilityCriteria);
      createMutation.mutate({
        ...formData,
        eligibilityCriteria: criteriaObj,
      });
    } catch (err) {
      toast.error("Invalid JSON in Eligibility Criteria.");
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <div className="mb-10">
        <h1 className="text-4xl font-bold tracking-tight text-foreground sm:text-5xl">
          Government Admin
        </h1>
        <p className="mt-3 text-lg text-muted-foreground">
          Manage welfare schemes and define JSON eligibility logic.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-12 lg:grid-cols-12">
        {/* CREATE FORM */}
        <div className="lg:col-span-5">
          <div className="rounded-[2.5rem] bg-white/80 p-8 shadow-card backdrop-blur-2xl border border-white/60">
            <h2 className="text-xl font-bold text-foreground mb-6 flex items-center gap-2">
              <PlusCircle className="size-5 text-primary" />
              Add New Scheme
            </h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              <div>
                <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">Scheme Name</label>
                <input
                  required
                  type="text"
                  className="w-full rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>
              <div>
                <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">Description</label>
                <textarea
                  required
                  rows={3}
                  className="w-full resize-none rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">State</label>
                  <input
                    required
                    type="text"
                    placeholder="e.g. Maharashtra"
                    className="w-full rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                    value={formData.state}
                    onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                  />
                </div>
                <div>
                  <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">Department</label>
                  <input
                    required
                    type="text"
                    className="w-full rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 text-[15px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                    value={formData.department}
                    onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                  />
                </div>
              </div>
              <div>
                <label className="text-sm font-semibold text-foreground/80 mb-1.5 block">Eligibility Rules (JSON)</label>
                <textarea
                  required
                  rows={6}
                  className="w-full resize-none rounded-2xl border border-black/10 bg-black/[0.02] px-4 py-3 font-mono text-[13px] focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary transition-all"
                  value={formData.eligibilityCriteria}
                  onChange={(e) => setFormData({ ...formData, eligibilityCriteria: e.target.value })}
                />
              </div>
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="mt-4 flex w-full items-center justify-center rounded-2xl bg-primary px-4 py-3.5 text-[15px] font-bold text-primary-foreground transition-transform hover:scale-[1.02] disabled:opacity-50"
              >
                {createMutation.isPending ? "Creating..." : "Publish Scheme"}
              </button>
            </form>
          </div>
        </div>

        {/* LIST SCHEMES */}
        <div className="lg:col-span-7 flex flex-col gap-4">
          <h2 className="text-sm font-bold tracking-wide text-foreground/80 pl-2 uppercase">
            Active Schemes Database
          </h2>
          {isLoading ? (
            <div className="h-40 flex items-center justify-center">
              <span className="text-muted-foreground font-medium animate-pulse">Loading schemes...</span>
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              {schemes?.map((scheme) => (
                <div key={scheme.id} className="group flex items-start justify-between gap-4 rounded-[2rem] bg-white/60 p-6 shadow-sm backdrop-blur-xl border border-white/60 transition-all hover:bg-white/90">
                  <div className="flex-1 min-w-0">
                    <h3 className="text-[17px] font-bold text-foreground mb-1 truncate">{scheme.name}</h3>
                    <p className="text-[14px] text-muted-foreground line-clamp-2 leading-relaxed">
                      {scheme.description}
                    </p>
                    <div className="mt-4 flex flex-wrap gap-2">
                      <span className="rounded-full bg-black/5 px-3 py-1 text-xs font-semibold text-foreground/80">
                        {scheme.state}
                      </span>
                      <span className="rounded-full bg-black/5 px-3 py-1 text-xs font-semibold text-foreground/80">
                        {scheme.department}
                      </span>
                    </div>
                  </div>
                  <button
                    onClick={() => {
                      if (confirm("Are you sure you want to delete this scheme?")) {
                        deleteMutation.mutate(scheme.id);
                      }
                    }}
                    className="flex size-10 shrink-0 items-center justify-center rounded-full bg-destructive/10 text-destructive opacity-0 transition-all hover:bg-destructive hover:text-white group-hover:opacity-100"
                    title="Delete Scheme"
                  >
                    <Trash2 className="size-4.5" />
                  </button>
                </div>
              ))}
              {schemes?.length === 0 && (
                <div className="rounded-[2rem] bg-white/40 p-12 text-center border border-dashed border-black/10">
                  <CheckCircle2 className="mx-auto size-12 text-muted-foreground/30 mb-3" />
                  <h3 className="text-lg font-bold text-foreground/70">No schemes found</h3>
                  <p className="text-sm text-muted-foreground">Add one using the form to get started.</p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
