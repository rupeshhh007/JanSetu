import Link from "next/link";
import type { LucideIcon } from "lucide-react";
import { FileText, Sparkles, ClipboardList, UserCircle } from "lucide-react";

interface QuickAction {
  title: string;
  href: string;
  icon: LucideIcon;
  colorClass: string;
}

const ACTIONS: QuickAction[] = [
  {
    title: "Schemes",
    href: "/schemes",
    icon: FileText,
    colorClass: "bg-blue-500 text-white",
  },
  {
    title: "Matches",
    href: "/recommendations",
    icon: Sparkles,
    colorClass: "bg-emerald-500 text-white",
  },
  {
    title: "Applications",
    href: "/applications",
    icon: ClipboardList,
    colorClass: "bg-purple-500 text-white",
  },
  {
    title: "Profile",
    href: "/profile",
    icon: UserCircle,
    colorClass: "bg-orange-500 text-white",
  },
];

export function QuickActions() {
  return (
    <div className="flex flex-col gap-4">
      <h2 className="text-sm font-semibold tracking-wide text-foreground/80 pl-2">
        Quick Actions
      </h2>
      <div className="grid grid-cols-2 gap-4">
        {ACTIONS.map((action) => (
          <Link
            key={action.href}
            href={action.href}
            className="flex flex-col items-center justify-center gap-3 aspect-square rounded-[2rem] bg-white/60 backdrop-blur-xl border border-white/60 shadow-[0_8px_30px_rgb(0,0,0,0.04)] hover:bg-white/80 transition-all duration-300 hover:scale-[1.02]"
          >
            <div className={`flex size-14 items-center justify-center rounded-2xl shadow-sm ${action.colorClass}`}>
              <action.icon className="size-6" strokeWidth={2.5} aria-hidden="true" />
            </div>
            <span className="font-medium text-[15px] text-foreground tracking-tight">
              {action.title}
            </span>
          </Link>
        ))}
      </div>
    </div>
  );
}
