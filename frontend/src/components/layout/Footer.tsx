const FOOTER_LINKS = [
  "Privacy-first",
  "Accessible by design",
  "Information, not a government order",
];

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="border-t-2 border-foreground bg-surface">
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-8 md:flex-row md:justify-between">
          {/* Brand + trust statement */}
          <div className="max-w-md">
            <p className="text-lg font-black uppercase text-text-primary">
              Jan<span className="text-primary">Setu</span>
            </p>
            <p className="mt-2 text-sm text-text-muted">
              Clear pathways to public welfare schemes. Check every final
              requirement with the official department before you apply.
            </p>
          </div>

          {/* Footer navigation */}
          <nav aria-label="Footer navigation">
            <ul className="grid grid-cols-1 gap-2">
              {FOOTER_LINKS.map((label) => (
                <li
                  key={label}
                  className="text-sm font-semibold text-text-muted"
                >
                  {label}
                </li>
              ))}
            </ul>
          </nav>
        </div>

        <div className="mt-8 border-t border-border pt-6">
          <p className="text-sm text-text-muted">
            © {currentYear} JanSetu. A citizen-support prototype. Scheme
            information must be verified with the official source.
          </p>
        </div>
      </div>
    </footer>
  );
}
