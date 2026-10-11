"use client";

import { useState, KeyboardEvent } from "react";
import { SendHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface ChatInputProps {
  onSend: (message: string) => void;
  disabled?: boolean;
}

export function ChatInput({ onSend, disabled = false }: ChatInputProps) {
  const [message, setMessage] = useState("");

  function send() {
    const value = message.trim();
    if (!value) return;

    onSend(value);
    setMessage("");
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      e.preventDefault();
      send();
    }
  }

  return (
    <div className="flex items-center gap-2 border-2 border-foreground bg-background p-1.5 pr-2 focus-within:ring-3 focus-within:ring-primary/40 transition-all">
      <Input
        aria-label="Ask JanSetu Guide"
        placeholder="Ask about a scheme or application"
        value={message}
        disabled={disabled}
        onChange={(e) => setMessage(e.target.value)}
        onKeyDown={handleKeyDown}
        className="h-12 border-none bg-transparent text-[16px] text-foreground placeholder:text-muted-foreground focus-visible:ring-0 px-4"
      />

      <Button
        onClick={send}
        disabled={disabled || !message.trim()}
        aria-label="Send message"
        className="h-10 w-10 shrink-0 rounded-none border-2 border-foreground bg-primary p-0 text-white hover:bg-primary-hover shadow-[2px_2px_0_#151515] disabled:opacity-50 disabled:bg-black/10 disabled:text-black/30 transition-all"
      >
        <SendHorizontal className="h-5 w-5" />
      </Button>
    </div>
  );
}
