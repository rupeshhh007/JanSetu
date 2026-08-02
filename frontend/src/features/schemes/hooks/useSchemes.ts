import { useQuery } from "@tanstack/react-query";

import { getSchemes } from "../api/schemesApi";

export function useSchemes() {
  return useQuery({
    queryKey: ["schemes"],
    queryFn: getSchemes,
  });
}