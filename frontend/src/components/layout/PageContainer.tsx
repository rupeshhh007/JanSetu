import { cn } from "@/lib/utils";

interface PageContainerProps {
  children: React.ReactNode;
  className?: string;
  /** Use "narrow" for forms/reading content, "wide" for dashboards/listings */
  width?: "narrow" | "default" | "wide";
}

const WIDTH_MAP = {
  narrow: "max-w-3xl",
  default: "max-w-5xl",
  wide: "max-w-7xl",
} as const;

export function PageContainer({
  children,
  className,
  width = "default",
}: PageContainerProps) {
  return (
    <div
      className={cn(
        "mx-auto w-full px-4 py-8 sm:px-6 sm:py-10 lg:px-8",
        WIDTH_MAP[width],
        className
      )}
    >
      {children}
    </div>
  );
}