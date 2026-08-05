"use client";

import { Bot } from "lucide-react";

export function TypingIndicator() {
  return (
    <div className="flex gap-3">
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-white">
        <Bot size={18} />
      </div>

      <div className="rounded-2xl border bg-card px-5 py-4 shadow">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium">
            JanSetu AI is thinking
          </span>

          <div className="flex gap-1">
            <span className="h-2 w-2 animate-bounce rounded-full bg-primary" />
            <span
              className="h-2 w-2 animate-bounce rounded-full bg-primary"
              style={{ animationDelay: "0.15s" }}
            />
            <span
              className="h-2 w-2 animate-bounce rounded-full bg-primary"
              style={{ animationDelay: "0.3s" }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}