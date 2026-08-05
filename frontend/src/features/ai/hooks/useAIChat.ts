import { useMutation } from "@tanstack/react-query";

import { chatWithAI } from "../api/aiApi";

export function useAIChat() {
  return useMutation({
    mutationFn: chatWithAI,
  });
}