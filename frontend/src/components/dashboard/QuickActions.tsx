import Link from "next/link";
import type { LucideIcon } from "lucide-react";
import { ArrowUpRight, FileText, Sparkles, ClipboardList, UserCircle } from "lucide-react";

import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";

interface QuickAction {
  title: string;
  description: string;
  href: string;
  icon: LucideIcon;
  accent: string;
}

const ACTIONS: QuickAction[] = [
  {
    title: "Browse Schemes",
    description: "Explore all welfare schemes",
    href: "/schemes",
    icon: FileText,
    accent: "bg-primary/10 text-primary",
  },
  {
    title: "Recommendations",
    description: "See schemes matched to you",
    href: "/recommendations",
    icon: Sparkles,
    accent: "bg-info/10 text-info",
  },
  {
    title: "My Applications",
    description: "Track application status",
    href: "/applications",
    icon: ClipboardList,
    accent: "bg-warning/10 text-warning",
  },
  {
    title: "Update Profile",
    description: "Keep your details current",
    href: "/profile",
    icon: UserCircle,
    accent: "bg-success/10 text-success",
  },
];

export function QuickActions() {
  return (
    <div>
      <h2 className="text-lg font-semibold tracking-tight">Quick actions</h2>

      <div className="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {ACTIONS.map((action) => (
          <Link key={action.href} href={action.href} className="group block">
            <Card
              className={cn(
                "h-full border border-border p-5 transition-all duration-200",
                "hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-[var(--shadow-card-hover)]"
              )}
            >
              <div className="flex items-start justify-between">
                <div
                  className={cn(
                    "flex size-10 items-center justify-center rounded-lg transition-transform duration-200 group-hover:scale-105",
                    action.accent
                  )}
                >
                  <action.icon className="size-5" strokeWidth={2} aria-hidden="true" />
                </div>

                <ArrowUpRight
                  className="size-4 text-muted-foreground opacity-0 transition-all duration-200 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:opacity-100"
                  aria-hidden="true"
                />
              </div>

              <p className="mt-4 text-sm font-semibold text-foreground">
                {action.title}
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                {action.description}
              </p>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
