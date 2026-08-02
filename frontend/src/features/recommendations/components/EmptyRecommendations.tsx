export function EmptyRecommendations() {
  return (
    <div className="rounded-lg border border-dashed p-12 text-center">
      <h2 className="text-xl font-semibold">
        No recommendations available
      </h2>

      <p className="mt-2 text-muted-foreground">
        Complete or update your profile to receive personalized scheme
        recommendations.
      </p>
    </div>
  );
}