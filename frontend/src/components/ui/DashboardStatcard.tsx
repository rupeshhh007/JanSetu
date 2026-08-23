import type { LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface Props {
  title: string;
  value: number | string;
  icon: LucideIcon;
  accent?: "primary" | "success" | "neutral";
  caption?: string;
  captionTone?: "positive" | "neutral";
}

const ACCENT_STYLES = {
  primary: "bg-blue-500/10 text-blue-600",
  success: "bg-emerald-500/10 text-emerald-600",
  neutral: "bg-slate-500/10 text-slate-600",
};

export function DashboardStatCard({
  title,
  value,
  icon: Icon,
  accent = "primary",
  caption,
  captionTone = "neutral",
}: Props) {
  const iconStyle = ACCENT_STYLES[accent];

  return (
    <div className="relative overflow-hidden rounded-[2rem] bg-white/60 backdrop-blur-xl border border-white/60 shadow-[0_8px_30px_rgb(0,0,0,0.04)] p-6 transition-transform hover:scale-[1.02] duration-300 cursor-default">
      <div className="flex items-start justify-between">
        <div className={cn("flex size-12 items-center justify-center rounded-2xl", iconStyle)}>
          <Icon className="size-6" strokeWidth={2} />
        </div>
        {caption && (
          <span className={cn(
            "text-xs font-medium px-3 py-1 rounded-full",
            captionTone === "positive" ? "bg-emerald-500/10 text-emerald-700" : "bg-gray-500/10 text-gray-600"
          )}>
            {caption}
          </span>
        )}
      </div>
      
      <div className="mt-8">
        <h3 className="text-4xl font-semibold tracking-tight text-foreground">{value}</h3>
        <p className="text-sm font-medium text-muted-foreground mt-1">{title}</p>
      </div>
    </div>
  );
}
