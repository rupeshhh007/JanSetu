"use client";
import { useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Menu,
  X,
  Landmark,
  LayoutDashboard,
  UserRound,
  Search,
  ClipboardList,
  ScanLine,
  LogOut,
  MessageCircle,
  ShieldCheck,
} from "lucide-react";
import { useAuthStore } from "@/store/useAuthStore";
import { cn } from "@/lib/utils";
export const NAV_LINKS = [
  { label: "Overview", href: "/dashboard", icon: LayoutDashboard },
  { label: "Discover schemes", href: "/schemes", icon: Search },
  { label: "My matches", href: "/recommendations", icon: ScanLine },
  { label: "Applications", href: "/applications", icon: ClipboardList },
  { label: "My profile", href: "/profile", icon: UserRound },
];
export function Navbar() {
  const [open, setOpen] = useState(false);
  const pathname = usePathname();
  const router = useRouter();
  const { role, logout } = useAuthStore();
  const links =
    role === "ADMIN"
      ? [
          { label: "Review desk", href: "/admin", icon: ShieldCheck },
          ...NAV_LINKS,
        ]
      : NAV_LINKS;
  return (
    <>
      <header className="sticky top-0 z-40 flex items-center justify-between border-b bg-[#163d32] px-5 py-3 text-white lg:hidden">
        <Link
          href="/dashboard"
          className="flex items-center gap-3 text-2xl editorial"
        >
          <Landmark size={25} />
          JanSetu
        </Link>
        <button
          className="p-2"
          aria-label={open ? "Close navigation" : "Open navigation"}
          aria-expanded={open}
          aria-controls="workspace-nav"
          onClick={() => setOpen(!open)}
        >
          {open ? <X /> : <Menu />}
        </button>
      </header>
      {open && (
        <button
          aria-label="Close navigation"
          className="fixed inset-0 top-16 z-30 bg-black/30 lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}
      <aside
        id="workspace-nav"
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex w-[248px] flex-col bg-[#163d32] px-5 py-8 text-white lg:translate-x-0",
          open ? "top-16 visible translate-x-0" : "invisible -translate-x-full lg:visible",
        )}
      >
        <Link
          href="/dashboard"
          className="mb-11 hidden items-center gap-3 px-3 text-[34px] editorial lg:flex"
        >
          <Landmark size={32} strokeWidth={1.4} />
          JanSetu
        </Link>
        <nav aria-label="Primary navigation" className="space-y-2">
          {links.map(({ label, href, icon: Icon }) => (
            <Link
              key={href}
              href={href}
              onClick={() => setOpen(false)}
              aria-current={
                pathname === href || pathname.startsWith(href + "/")
                  ? "page"
                  : undefined
              }
              className={cn(
                "flex min-h-12 items-center gap-3 rounded-lg px-4 text-sm transition-colors",
                pathname === href || pathname.startsWith(href + "/")
                  ? "bg-white/12 text-white"
                  : "text-white/75 hover:bg-white/6 hover:text-white",
              )}
            >
              <Icon size={19} strokeWidth={1.6} />
              {label}
            </Link>
          ))}
        </nav>
        <div className="mt-auto pt-10">
          <div className="border-t border-white/20 px-2 pt-6">
            <p className="editorial mb-5 text-xl text-white/90">
              A little guidance
              <br />
              goes a long way.
            </p>
            <button
              className="flex min-h-11 w-full items-center justify-center gap-2 rounded-lg border border-white/30 text-sm hover:bg-white/10"
              onClick={() => {
                setOpen(false);
                window.dispatchEvent(new Event("jansetu:guide"));
              }}
            >
              <MessageCircle size={18} />
              Ask JanSetu Guide
            </button>
          </div>
          <button
            className="mt-5 flex min-h-11 items-center gap-3 px-4 text-sm text-white/70 hover:text-white"
            onClick={() => {
              logout();
              router.replace("/login");
            }}
          >
            <LogOut size={18} />
            Sign out
          </button>
        </div>
      </aside>
    </>
  );
}
