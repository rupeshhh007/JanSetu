import Link from "next/link";

const FOOTER_LINKS = [
  { label: "About JanSetu", href: "/about" },
  { label: "Privacy Policy", href: "/privacy" },
  { label: "Terms of Use", href: "/terms" },
  { label: "Accessibility Statement", href: "/accessibility" },
  { label: "Contact Us", href: "/contact" },
];

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="border-t border-border bg-surface">
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8 md:flex-row md:justify-between">
          {/* Brand + trust statement */}
          <div className="max-w-md">
            <p className="text-lg font-semibold text-text-primary">
              <span className="text-primary">Jan</span>Setu
            </p>
            <p className="mt-2 text-sm text-text-muted">
              An initiative to connect every citizen with the government
              welfare schemes they are entitled to — securely and
              transparently.
            </p>
          </div>

          {/* Footer navigation */}
          <nav aria-label="Footer navigation">
            <ul className="grid grid-cols-1 gap-x-8 gap-y-3 sm:grid-cols-2">
              {FOOTER_LINKS.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="flex min-h-touch items-center text-sm font-medium text-text-muted hover:text-text-primary hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </div>

        <div className="mt-8 border-t border-border pt-6">
          <p className="text-sm text-text-muted">
            © {currentYear} JanSetu. A Digital Public Good initiative. All
            content is available under the National Open Data License.
          </p>
        </div>
      </div>
    </footer>
  );
}