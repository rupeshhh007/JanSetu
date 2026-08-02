import type { SchemeResponse } from "../types";
import { SchemeCard } from "./SchemeCard";

interface SchemeGridProps {
  schemes: SchemeResponse[];
}

export function SchemeGrid({ schemes }: SchemeGridProps) {
  return (
    <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      {schemes.map((scheme) => (
        <SchemeCard
          key={scheme.id}
          scheme={scheme}
        />
      ))}
    </div>
  );
}