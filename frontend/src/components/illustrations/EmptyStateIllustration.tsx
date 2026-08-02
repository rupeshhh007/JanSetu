import type { LucideIcon } from "lucide-react";

interface EmptyStateIllustrationProps {
  icon: LucideIcon;
}

/**
 * A restrained, on-brand illustration used behind empty-state icons.
 * Pure inline SVG (no external assets), themed entirely with CSS variables
 * so it always matches the app's palette.
 */
export function EmptyStateIllustration({ icon: Icon }: EmptyStateIllustrationProps) {
  return (
    <div className="relative mx-auto mb-5 flex size-20 items-center justify-center">
      <svg
        viewBox="0 0 96 96"
        className="absolute inset-0 size-full text-primary"
        aria-hidden="true"
      >
        <circle cx="48" cy="48" r="47" fill="currentColor" opacity="0.06" />
        <circle
          cx="48"
          cy="48"
          r="35"
          fill="none"
          stroke="currentColor"
          strokeOpacity="0.15"
          strokeWidth="1.5"
          strokeDasharray="4 5"
        />
      </svg>

      <div className="relative flex size-14 items-center justify-center rounded-2xl border border-border bg-card shadow-[var(--shadow-card)]">
        <Icon className="size-6 text-primary" strokeWidth={1.75} aria-hidden="true" />
      </div>
    </div>
  );
}
