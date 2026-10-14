"use client";
import Link from "next/link";
import { ArrowRight, Bookmark, Check, MapPin } from "lucide-react";
import { useSavedSchemeIds, useToggleSavedScheme } from "@/features/bookmarks/hooks";
import type { SchemeResponse } from "../types";
import { schemeSourceFreshness } from "../sourceFreshness";
export function SchemeCard({
  scheme,
  compared,
  onCompare,
}: {
  scheme: SchemeResponse;
  compared?: boolean;
  onCompare?: () => void;
}) {
  const savedQuery = useSavedSchemeIds();
  const saved = (savedQuery.data ?? []).includes(scheme.id);
  const source = schemeSourceFreshness(scheme.lastVerifiedAt);
  const toggle = useToggleSavedScheme();
  return (
    <article className="flex h-full flex-col rounded-xl border bg-white p-6 transition-shadow hover:shadow-card-hover">
      <div className="mb-4 flex items-center justify-between gap-2">
        <span className="flex items-center gap-1.5 text-xs text-muted-foreground">
          <MapPin size={14} />
          {scheme.state}
        </span>
        <button
          aria-label={`${saved ? "Unsave" : "Save"} ${scheme.name}`}
          aria-pressed={saved}
          disabled={toggle.isPending || savedQuery.isPending || savedQuery.isError}
          onClick={() => toggle.mutate({ schemeId: scheme.id, saved })}
          className="-mr-2 rounded-lg p-2 text-primary hover:bg-muted"
        >
          <Bookmark size={19} fill={saved ? "currentColor" : "none"} />
        </button>
      </div>
      <p className="mb-2 text-[11px] font-medium text-primary">
        {scheme.department}
      </p>
      <h2 className="text-lg leading-snug">
        <Link href={`/schemes/${scheme.id}`} className="hover:text-primary">
          {scheme.name}
        </Link>
      </h2>
      <p className="mt-3 line-clamp-3 text-sm leading-6 text-muted-foreground">
        {scheme.description}
      </p>
      <p className={`mt-3 text-xs ${source.status === "stale" ? "text-amber-800" : "text-muted-foreground"}`}>
        {source.text}
      </p>
      {scheme.benefits[0] && (
        <div className="my-5 flex gap-2 rounded-lg bg-muted/70 p-3 text-xs leading-5">
          <Check className="mt-0.5 shrink-0 text-primary" size={15} />
          <p>{scheme.benefits[0]}</p>
        </div>
      )}
      <div className="mt-auto flex items-center justify-between gap-2 border-t pt-4">
        <Link className="text-link" href={`/schemes/${scheme.id}`}>
          View details <ArrowRight size={15} />
        </Link>
        {onCompare && (
          <button
            onClick={onCompare}
            aria-pressed={compared}
            className={`rounded-lg border px-3 py-2 text-xs transition-colors ${compared ? "border-primary bg-accent text-primary" : "hover:bg-muted"}`}
          >
            {compared ? "Added to compare" : "Compare"}
          </button>
        )}
      </div>
    </article>
  );
}
