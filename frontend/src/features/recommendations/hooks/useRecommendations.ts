import { useProfile } from "@/features/citizen/hooks/useProfile";
import { useQuery } from "@tanstack/react-query";

import { getRecommendations } from "../api/recommendationsApi";

export function useRecommendations() {
  const profile = useProfile();
  return useQuery({
    enabled: !!profile.data,
    queryKey: ["recommendations"],
    queryFn: getRecommendations,
  });
}