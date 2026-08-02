import type { LucideIcon } from "lucide-react";

import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

interface Props {
  title: string;
  value: number | string;
  icon: LucideIcon;
  accent?: "primary" | "success" | "warning" | "info";
}

const ACCENT_STYLES: Record<
  NonNullable<Props["accent"]>,
  { icon: string; glow: string }
> = {
  primary: {
    icon: "bg-primary/10 text-primary",
    glow: "from-primary/10",
  },
  success: {
    icon: "bg-success/10 text-success",
    glow: "from-success/10",
  },
  warning: {
    icon: "bg-warning/10 text-warning",
    glow: "from-warning/10",
  },
  info: {
    icon: "bg-info/10 text-info",
    glow: "from-info/10",
  },
};

export function DashboardStatCard({
  title,
  value,
  icon: Icon,
  accent = "primary",
}: Props) {
  const styles = ACCENT_STYLES[accent];

  return (
    <Card
      className={cn(
        "group relative overflow-hidden border border-border transition-all duration-200",
        "hover:-translate-y-0.5 hover:shadow-[var(--shadow-card-hover)]"
      )}
    >
      {/* subtle corner gradient — decorative only */}
      <div
        className={cn(
          "pointer-events-none absolute -right-6 -top-6 size-28 rounded-full bg-gradient-to-br to-transparent opacity-70 blur-2xl transition-opacity duration-200 group-hover:opacity-100",
          styles.glow
        )}
        aria-hidden="true"
      />

      <CardContent className="relative flex items-start justify-between gap-4 p-6">
        <div className="min-w-0">
          <p className="text-sm font-medium text-muted-foreground">{title}</p>
          <p className="mt-2 text-3xl font-semibold tracking-tight text-foreground">
            {value}
          </p>
        </div>

        <div
          className={cn(
            "flex size-11 shrink-0 items-center justify-center rounded-xl transition-transform duration-200 group-hover:scale-105",
            styles.icon
          )}
        >
          <Icon className="size-5" strokeWidth={2} aria-hidden="true" />
        </div>
      </CardContent>
    </Card>
  );
}
