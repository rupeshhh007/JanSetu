import { Landmark } from "lucide-react";

interface DashboardHeroProps {
  /** First name only — purely presentational, no data fetching here. */
  firstName?: string;
}

/** Pure presentation helpers. No state, no side effects, no API calls. */
function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}

function getTodayLabel(): string {
  return new Date().toLocaleDateString("en-GB", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

export function DashboardHero({ firstName }: DashboardHeroProps) {
  const greeting = getGreeting();
  const today = getTodayLabel();

  return (
    <div className="relative overflow-hidden rounded-3xl border border-border bg-card/80 px-6 py-12 backdrop-blur-xl sm:px-10 sm:py-16">
      {/* Subtle grid texture — decorative only, faded out toward the edges */}
      <div
        className="pointer-events-none absolute inset-0 opacity-40"
        style={{
          backgroundImage:
            "linear-gradient(to right, rgb(15 23 42 / 0.05) 1px, transparent 1px), linear-gradient(to bottom, rgb(15 23 42 / 0.05) 1px, transparent 1px)",
          backgroundSize: "32px 32px",
          maskImage:
            "radial-gradient(ellipse 80% 80% at 50% 0%, black 40%, transparent 100%)",
          WebkitMaskImage:
            "radial-gradient(ellipse 80% 80% at 50% 0%, black 40%, transparent 100%)",
        }}
        aria-hidden="true"
      />

      {/* Decorative glows */}
      <div
        className="pointer-events-none absolute -right-24 -top-24 size-72 rounded-full bg-primary/10 blur-3xl"
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute -bottom-28 -left-16 size-64 rounded-full bg-emerald-500/[0.08] blur-3xl"
        aria-hidden="true"
      />

      <div className="relative max-w-2xl">
        <div className="flex flex-wrap items-center gap-3">
          <span className="inline-flex items-center gap-1.5 rounded-full border border-border bg-surface/80 px-3 py-1 text-xs font-medium text-muted-foreground backdrop-blur">
            <Landmark className="size-3.5 text-primary" aria-hidden="true" />
            JanSetu Citizen Dashboard
          </span>
          <span className="text-xs font-medium text-muted-foreground">
            {today}
          </span>
        </div>

        <h1 className="mt-5 text-4xl font-bold tracking-tight text-foreground sm:text-5xl">
          {greeting}
          {firstName ? `, ${firstName}` : ""}
        </h1>

        <p className="mt-3 max-w-lg text-base leading-relaxed text-muted-foreground">
          Here&apos;s an overview of your JanSetu account — schemes, matches,
          and applications, all in one place.
        </p>
      </div>
    </div>
  );
}
