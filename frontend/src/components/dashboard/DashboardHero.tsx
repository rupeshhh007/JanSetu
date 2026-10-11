interface DashboardHeroProps {
  firstName?: string;
}

function getGreeting(): string {
  const hour = new Date().getHours();
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}

function getTodayLabel(): string {
  return new Date().toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
}

export function DashboardHero({ firstName }: DashboardHeroProps) {
  const greeting = getGreeting();
  const today = getTodayLabel();

  return (
    <div className="border-y-2 border-foreground py-5 sm:flex sm:items-end sm:justify-between">
      <div>
        <p className="noticeboard-kicker text-primary">Your citizen desk</p>
        <h1 className="mt-1 text-4xl leading-none sm:text-6xl">
          {greeting}
          {firstName ? `, ${firstName}` : ""}.
        </h1>
      </div>
      <p className="mt-4 max-w-xs border-l-4 border-accent pl-3 font-mono text-xs font-bold uppercase tracking-wide text-text-muted sm:mt-0">
        {today} · one clear next step at a time
      </p>
    </div>
  );
}
