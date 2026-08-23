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
    <div className="flex items-center gap-3 rounded-full border border-black/5 bg-white shadow-sm p-2">
      <Input
        placeholder="Ask JanSetu AI..."
        value={message}
        disabled={disabled}
        onChange={(e) => setMessage(e.target.value)}
        onKeyDown={handleKeyDown}
        className="h-10 border-none bg-transparent text-[15px] text-foreground placeholder:text-muted-foreground focus-visible:ring-0 px-4"
      />

      <Button
        onClick={send}
        disabled={disabled || !message.trim()}
        className="h-10 w-10 shrink-0 rounded-full bg-primary p-0 text-white hover:bg-primary-hover shadow-sm"
      >
        <SendHorizontal className="h-4 w-4" />
      </Button>
    </div>
  );
}