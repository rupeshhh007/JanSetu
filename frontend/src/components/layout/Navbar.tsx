"use client";

import { useState } from "react";
import Link from "next/link";
import { Menu, X } from "lucide-react";
import { cn } from "@/lib/utils";

const NAV_LINKS = [
  { label: "Home", href: "/" },
  { label: "Schemes", href: "/schemes" },
  { label: "Track Application", href: "/applications" },
  { label: "Help & Support", href: "/help" },
];

export function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-surface">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Wordmark — text-based, no icon dependency for trust/clarity */}
        <Link
          href="/"
          className="flex min-h-touch items-center gap-2 rounded-md px-1 text-xl font-semibold text-text-primary focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        >
          <span className="text-primary">Jan</span>Setu
        </Link>

        {/* Desktop nav */}
        <nav
          aria-label="Primary navigation"
          className="hidden items-center gap-1 md:flex"
        >
          {NAV_LINKS.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className="flex min-h-touch items-center rounded-md px-4 text-base font-medium text-text-primary hover:bg-background focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
            >
              {link.label}
            </Link>
          ))}
        </nav>

        {/* Desktop sign-in action */}
        <div className="hidden md:block">
          <Link
            href="/login"
            className="flex min-h-touch items-center rounded-lg bg-primary px-5 text-base font-medium text-primary-foreground hover:bg-primary-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          >
            Sign In
          </Link>
        </div>

        {/* Mobile menu toggle */}
        <button
          type="button"
          onClick={() => setIsMenuOpen((prev) => !prev)}
          aria-expanded={isMenuOpen}
          aria-controls="mobile-nav-menu"
          aria-label={isMenuOpen ? "Close navigation menu" : "Open navigation menu"}
          className="flex min-h-touch min-w-touch items-center justify-center rounded-md border border-border text-text-primary focus:outline-none focus-visible:ring-2 focus-visible:ring-primary md:hidden"
        >
          {isMenuOpen ? <X size={22} aria-hidden="true" /> : <Menu size={22} aria-hidden="true" />}
        </button>
      </div>

      {/* Mobile nav panel — simple show/hide, no slide/fade animation */}
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
              className="flex min-h-touch items-center rounded-md px-3 text-base font-medium text-text-primary hover:bg-background focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
            >
              {link.label}
            </Link>
          ))}
          <Link
            href="/login"
            onClick={() => setIsMenuOpen(false)}
            className="mt-2 flex min-h-touch items-center justify-center rounded-lg bg-primary px-5 text-base font-medium text-primary-foreground hover:bg-primary-hover focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
          >
            Sign In
          </Link>
        </div>
      </nav>
    </header>
  );
}