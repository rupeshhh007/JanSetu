"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Menu,
  X,
  Landmark,
  LayoutDashboard,
  UserCircle,
  FileText,
  ClipboardList,
  Sparkles,
  LogOut,
} from "lucide-react";

import { cn } from "@/lib/utils";
import { useAuthStore } from "@/store/useAuthStore";

const NAV_LINKS = [
  {
    label: "Dashboard",
    href: "/dashboard",
    icon: LayoutDashboard,
  },
  {
    label: "Profile",
    href: "/profile",
    icon: UserCircle,
  },
  {
    label: "Schemes",
    href: "/schemes",
    icon: FileText,
  },
  {
    label: "Applications",
    href: "/applications",
    icon: ClipboardList,
  },
  {
    href: "/recommendations",
    label: "Recommendations",
    icon: Sparkles,
  },
];

export function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const pathname = usePathname();
  const router = useRouter();

  const logout = useAuthStore((state) => state.logout);

  function handleLogout() {
    logout();
    setIsMenuOpen(false);
    router.replace("/login");
  }

  return (
    <header className="sticky top-0 z-50 w-full border-b border-black/5 bg-white/70 backdrop-blur-2xl">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <Link
          href="/dashboard"
          className="flex min-h-touch items-center gap-2.5 rounded-md px-1 text-lg font-semibold text-text-primary focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        >
          <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Landmark className="size-4.5" strokeWidth={2.25} aria-hidden="true" />
          </span>
          <span>
            <span className="text-primary">Jan</span>Setu
          </span>
        </Link>

        <nav
          aria-label="Primary navigation"
          className="hidden items-center gap-1 md:flex"
        >
          {NAV_LINKS.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={cn(
                "flex min-h-touch items-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                pathname === link.href
                  ? "text-primary"
                  : "text-muted-foreground hover:bg-muted/50 hover:text-foreground"
              )}
            >
              <link.icon className="size-4" strokeWidth={2} aria-hidden="true" />
              {link.label}
            </Link>
          ))}
        </nav>

        <div className="hidden md:block">
          <button
            type="button"
            onClick={handleLogout}
            className="flex min-h-touch items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          >
            <LogOut className="size-4" strokeWidth={2} aria-hidden="true" />
            Logout
          </button>
        </div>

        <button
          type="button"
          onClick={() => setIsMenuOpen((prev) => !prev)}
          aria-expanded={isMenuOpen}
          aria-controls="mobile-nav-menu"
          aria-label={
            isMenuOpen
              ? "Close navigation menu"
              : "Open navigation menu"
          }
          className="flex min-h-touch min-w-touch items-center justify-center rounded-md border border-border text-text-primary focus:outline-none focus-visible:ring-2 focus-visible:ring-primary md:hidden"
        >
          {isMenuOpen ? (
            <X size={22} aria-hidden="true" />
          ) : (
            <Menu size={22} aria-hidden="true" />
          )}
        </button>
      </div>

      <nav
        id="mobile-nav-menu"
        aria-label="Mobile navigation"
        className={cn(
          "border-t border-border bg-surface md:hidden",
          isMenuOpen ? "block" : "hidden"
        )}
      >
        <div className="flex flex-col gap-1 px-4 py-3">
          {NAV_LINKS.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              onClick={() => setIsMenuOpen(false)}
              className={cn(
                "flex min-h-touch items-center gap-2.5 rounded-lg px-3 py-2 text-base font-medium transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                pathname === link.href
                  ? "text-primary"
                  : "text-text-primary hover:bg-muted/50"
              )}
            >
              <link.icon className="size-4.5" strokeWidth={2} aria-hidden="true" />
              {link.label}
            </Link>
          ))}

          <button
            type="button"
            onClick={handleLogout}
            className="mt-2 flex min-h-touch items-center justify-center gap-2 rounded-lg bg-primary px-5 py-2 text-base font-medium text-primary-foreground hover:bg-primary-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          >
            <LogOut className="size-4.5" strokeWidth={2} aria-hidden="true" />
            Logout
          </button>
        </div>
      </nav>
    </header>
  );
}
