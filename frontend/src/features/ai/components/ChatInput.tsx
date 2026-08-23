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
    <div className="flex items-center gap-2 rounded-full bg-secondary/70 p-1.5 pr-2 focus-within:bg-secondary focus-within:ring-1 focus-within:ring-black/5 transition-all">
      <Input
        placeholder="Ask Gemini..."
        value={message}
        disabled={disabled}
        onChange={(e) => setMessage(e.target.value)}
        onKeyDown={handleKeyDown}
        className="h-12 border-none bg-transparent text-[16px] text-foreground placeholder:text-muted-foreground focus-visible:ring-0 px-4"
      />

      <Button
        onClick={send}
        disabled={disabled || !message.trim()}
        className="h-10 w-10 shrink-0 rounded-full bg-primary p-0 text-white hover:bg-primary-hover shadow-sm disabled:opacity-50 disabled:bg-black/10 disabled:text-black/30 transition-all"
      >
        <SendHorizontal className="h-5 w-5" />
      </Button>
    </div>
  );
}