import { useQuery } from "@tanstack/react-query";

import { getRecommendations } from "../api/recommendationsApi";

export function useRecommendations() {
  return useQuery({
    queryKey: ["recommendations"],
    queryFn: getRecommendations,
  });
}