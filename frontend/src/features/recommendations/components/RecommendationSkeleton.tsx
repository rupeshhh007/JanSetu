import { Card, CardContent, CardHeader } from "@/components/ui/card";

export function RecommendationSkeleton() {
  return (
    <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      {Array.from({ length: 6 }).map((_, index) => (
        <Card key={index}>
          <CardHeader>
            <div className="h-6 w-40 animate-pulse rounded bg-muted" />
            <div className="mt-3 h-4 w-full animate-pulse rounded bg-muted" />
            <div className="mt-2 h-4 w-2/3 animate-pulse rounded bg-muted" />
          </CardHeader>

          <CardContent className="space-y-3">
            <div className="h-4 w-28 animate-pulse rounded bg-muted" />

            {Array.from({ length: 4 }).map((_, i) => (
              <div
                key={i}
                className="h-4 animate-pulse rounded bg-muted"
              />
            ))}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}