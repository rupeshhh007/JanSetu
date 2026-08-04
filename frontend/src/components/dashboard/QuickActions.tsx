import Link from "next/link";
import type { LucideIcon } from "lucide-react";
import {
  ArrowUpRight,
  FileText,
  Sparkles,
  ClipboardList,
  UserCircle,
} from "lucide-react";

import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";

interface QuickAction {
  title: string;
  description: string;
  href: string;
  icon: LucideIcon;
  iconClass: string;
  glowClass: string;
}

const ACTIONS: QuickAction[] = [
  {
    title: "Browse Schemes",
    description: "Explore all welfare schemes",
    href: "/schemes",
    icon: FileText,
    iconClass:
      "bg-gradient-to-br from-blue-500/15 via-blue-500/10 to-transparent text-primary ring-primary/15",
    glowClass: "from-primary/10",
  },
  {
    title: "Recommendations",
    description: "See schemes matched to you",
    href: "/recommendations",
    icon: Sparkles,
    iconClass:
      "bg-gradient-to-br from-emerald-500/15 via-emerald-500/10 to-transparent text-success ring-success/15",
    glowClass: "from-success/10",
  },
  {
    title: "My Applications",
    description: "Track application status",
    href: "/applications",
    icon: ClipboardList,
    iconClass:
      "bg-gradient-to-br from-blue-500/15 via-blue-500/10 to-transparent text-primary ring-primary/15",
    glowClass: "from-primary/10",
  },
  {
    title: "Update Profile",
    description: "Keep your details current",
    href: "/profile",
    icon: UserCircle,
    iconClass:
      "bg-gradient-to-br from-emerald-500/15 via-emerald-500/10 to-transparent text-success ring-success/15",
    glowClass: "from-success/10",
  },
];

export function QuickActions() {
  return (
    <div>
      <div className="flex items-baseline justify-between">
        <h2 className="text-lg font-semibold tracking-tight text-foreground">
          Quick actions
        </h2>
        <p className="hidden text-sm text-muted-foreground sm:block">
          Jump back in
        </p>
      </div>

      <div className="mt-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {ACTIONS.map((action) => (
          <Link
            key={action.href}
            href={action.href}
            className="group block rounded-2xl focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          >
            <Card
              className={cn(
                "relative h-full overflow-hidden border border-border p-6 transition-all duration-300 ease-out",
                "hover:-translate-y-1 hover:border-foreground/15 hover:shadow-[var(--shadow-card-hover)]"
              )}
            >
              <div
                className={cn(
                  "pointer-events-none absolute -right-10 -top-10 size-32 rounded-full bg-gradient-to-br to-transparent opacity-0 blur-2xl transition-opacity duration-300 group-hover:opacity-70",
                  action.glowClass
                )}
                aria-hidden="true"
              />

              <div className="relative flex items-start justify-between">
                <div
                  className={cn(
                    "flex size-11 items-center justify-center rounded-xl ring-1 transition-transform duration-300 ease-out group-hover:scale-105",
                    action.iconClass
                  )}
                >
                  <action.icon
                    className="size-5"
                    strokeWidth={2}
                    aria-hidden="true"
                  />
                </div>

                <ArrowUpRight
                  className="size-4 text-muted-foreground opacity-0 transition-all duration-300 ease-out group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:opacity-100"
                  aria-hidden="true"
                />
              </div>

              <p className="relative mt-5 text-sm font-semibold text-foreground">
                {action.title}
              </p>
              <p className="relative mt-1 text-sm text-muted-foreground">
                {action.description}
              </p>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
