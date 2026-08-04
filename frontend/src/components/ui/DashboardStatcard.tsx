import type { LucideIcon } from "lucide-react";

import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

interface Props {
  title: string;
  value: number | string;
  icon: LucideIcon;
  /** Restricted to the dashboard's palette: neutral, blue, emerald. */
  accent?: "primary" | "success" | "neutral";
  /**
   * Optional caption derived from real, already-fetched data
   * (e.g. "2 pending review", "Updated 3h ago") — never a fabricated metric.
   */
  caption?: string;
  captionTone?: "positive" | "neutral";
}

const ACCENT_STYLES: Record<
  NonNullable<Props["accent"]>,
  { icon: string; ring: string; glow: string }
> = {
  primary: {
    icon: "bg-gradient-to-br from-blue-500/15 via-blue-500/10 to-transparent text-primary",
    ring: "ring-primary/15",
    glow: "from-primary/10",
  },
  success: {
    icon: "bg-gradient-to-br from-emerald-500/15 via-emerald-500/10 to-transparent text-success",
    ring: "ring-success/15",
    glow: "from-success/10",
  },
  neutral: {
    icon: "bg-gradient-to-br from-slate-500/10 via-slate-500/5 to-transparent text-foreground/70",
    ring: "ring-border",
    glow: "from-slate-500/5",
  },
};

export function DashboardStatCard({
  title,
  value,
  icon: Icon,
  accent = "primary",
  caption,
  captionTone = "neutral",
}: Props) {
  const styles = ACCENT_STYLES[accent];

  return (
    <Card
      className={cn(
        "group relative overflow-hidden border border-border transition-all duration-300 ease-out",
        "hover:-translate-y-1 hover:border-foreground/15 hover:shadow-[var(--shadow-card-hover)]"
      )}
    >
      {/* subtle corner gradient — decorative only */}
      <div
        className={cn(
          "pointer-events-none absolute -right-8 -top-8 size-32 rounded-full bg-gradient-to-br to-transparent opacity-60 blur-2xl transition-opacity duration-300 group-hover:opacity-100",
          styles.glow
        )}
        aria-hidden="true"
      />

      <CardContent className="relative flex items-start justify-between gap-4 p-7">
        <div className="min-w-0">
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className="mt-2.5 text-[2rem] leading-none font-semibold tracking-tight text-foreground tabular-nums">
            {value}
          </p>

          {caption && (
            <div className="mt-3 flex items-center gap-1.5">
              <span
                className={cn(
                  "size-1.5 shrink-0 rounded-full",
                  captionTone === "positive"
                    ? "bg-success"
                    : "bg-muted-foreground/40"
                )}
                aria-hidden="true"
              />
              <span className="text-xs font-medium text-muted-foreground">
                {caption}
              </span>
            </div>
          )}
        </div>

        <div
          className={cn(
            "flex size-12 shrink-0 items-center justify-center rounded-2xl ring-1 transition-transform duration-300 ease-out group-hover:scale-105 group-hover:-rotate-3",
            styles.icon,
            styles.ring
          )}
        >
          <Icon className="size-5" strokeWidth={2} aria-hidden="true" />
        </div>
      </CardContent>
    </Card>
  );
}
