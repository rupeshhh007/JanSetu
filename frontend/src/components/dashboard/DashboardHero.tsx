import { Landmark } from "lucide-react";

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
    <div className="flex flex-col gap-2 pb-2">
      <p className="text-sm font-semibold tracking-widest uppercase text-muted-foreground/80 ml-1">
        {today}
      </p>
      <h1 className="text-4xl sm:text-5xl lg:text-6xl font-semibold tracking-tight text-foreground">
        {greeting}{firstName ? `, ${firstName}` : ""}
      </h1>
    </div>
  );
}
