import { useQuery } from "@tanstack/react-query";

import { getRecommendations } from "../api/recommendationsApi";

export function useRecommendations(enabled: boolean) {
  return useQuery({
    enabled,
    queryKey: ["recommendations"],
    queryFn: getRecommendations,
  });
}
