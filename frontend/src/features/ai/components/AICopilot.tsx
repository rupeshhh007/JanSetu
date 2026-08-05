// src/features/ai/components/AICopilot.tsx
"use client";

import React, { useState, useRef, useEffect, useCallback } from "react";
import { Bot, X, Sparkles } from "lucide-react";
import { useAIChat } from "../hooks/useAIChat";
import { ChatMessage as ChatMessageComponent } from "./ChatMessage";
import { ChatInput } from "./ChatInput";
import { TypingIndicator } from "./TypingIndicator";
import type { ChatMessage } from "../types";
import EmptyState from "./EmptyState";

export function AICopilot() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);

  const panelRef = useRef<HTMLDivElement>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const mutation = useAIChat();

  // Auto-scroll to newest message
  const scrollToBottom = useCallback(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, mutation.isPending, scrollToBottom]);

  // Accessibility: Escape key to close
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isOpen) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      window.addEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "hidden"; // Prevent background scrolling on mobile
    }

    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      document.body.style.overflow = "unset";
    };
  }, [isOpen]);

  const handleSendMessage = async (content: string) => {
    if (!content.trim() || mutation.isPending) return;

    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: "user",
      content: content.trim(),
    };

    setMessages((prev) => [...prev, userMessage]);

    try {
      const response = await mutation.mutateAsync({ message: content.trim() });

      const assistantMessage: ChatMessage = {
        id: crypto.randomUUID(),
        role: "assistant",
        content: response.response,
      };

      setMessages((prev) => [...prev, assistantMessage]);
    } catch (error) {
      const errorMessage: ChatMessage = {
        id: crypto.randomUUID(),
        role: "assistant",
        content:
          "I encountered an error connecting to the JanSetu servers. Please try again.",
      };
      setMessages((prev) => [...prev, errorMessage]);
    }
  };

  return (
    <>
      {/* Floating Action Button */}
      <button
        onClick={() => setIsOpen(true)}
        aria-label="Open AI Copilot"
        aria-expanded={isOpen}
        aria-controls="ai-copilot-panel"
        className={`
          fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center 
          rounded-2xl bg-black text-white hover:bg-zinc-800 shadow-2xl ring-1 ring-zinc-800 
          transition-all duration-300 ease-out hover:scale-105 
          dark:bg-zinc-50 dark:text-zinc-900 dark:ring-white/10
          ${isOpen ? "scale-0 opacity-0 pointer-events-none" : "scale-100 opacity-100"}
        `}
      >
        <Bot size={24} className="animate-pulse duration-3000" />
      </button>

      {/* Backdrop Overlay */}
      <div
        aria-hidden="true"
        onClick={() => setIsOpen(false)}
        className={`
          fixed inset-0 z-50 bg-black/40 backdrop-blur-sm transition-opacity duration-500
          ${isOpen ? "opacity-100" : "pointer-events-none opacity-0"}
        `}
      />

      {/* Slide-over Panel */}
      <div
        id="ai-copilot-panel"
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-label="JanSetu AI Assistant"
        className={`
          fixed bottom-0 right-0 top-0 z-50 flex h-[100dvh] w-full flex-col 
          bg-zinc-950 shadow-2xl 
          transition-transform duration-500 ease-[cubic-bezier(0.32,0.72,0,1)]
          sm:w-[460px] sm:rounded-l-2xl sm:border-l sm:border-zinc-800
          ${isOpen ? "translate-x-0" : "translate-x-full"}
        `}
      >
        {/* Header */}
        <header className="sticky top-0 z-10 flex items-center justify-between border-b border-zinc-800 bg-zinc-950/95 px-6 py-4 backdrop-blur-xl">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl border border-zinc-700 bg-zinc-900">
              <Sparkles size={18} className="text-zinc-100" />
            </div>
            <div className="flex flex-col">
              <h2 className="text-base font-semibold text-zinc-100 tracking-tight">
                JanSetu AI
              </h2>
              <p className="text-xs text-zinc-400">
                Personal Welfare Assistant
              </p>
            </div>
          </div>
          <button
            onClick={() => setIsOpen(false)}
            aria-label="Close panel"
            className="rounded-full p-2 text-zinc-400 transition-colors hover:bg-zinc-800 hover:text-zinc-100"
          >
            <X size={20} />
          </button>
        </header>

        {/* Scrollable Body */}
        <div className="flex-1 overflow-y-auto px-5 py-6 scroll-smooth">
          {messages.length === 0 ? (
            <div className="flex h-full flex-col justify-end pb-4 animate-in fade-in slide-in-from-bottom-4 duration-700">
              <EmptyState onPromptClick={handleSendMessage} />
            </div>
          ) : (
            <div className="flex flex-col gap-6">
              {messages.map((msg) => (
                <div
                  key={msg.id}
                  className="animate-in fade-in slide-in-from-bottom-2 duration-300"
                >
                  <ChatMessageComponent message={msg} />
                </div>
              ))}

              {mutation.isPending && (
                <div className="animate-in fade-in duration-300">
                  <TypingIndicator />
                </div>
              )}

              {/* Invisible element to auto-scroll to */}
              <div ref={messagesEndRef} className="h-px w-full" />
            </div>
          )}
        </div>

        {/* Footer Input Area */}
        <div className="flex-shrink-0 border-t border-zinc-800 bg-zinc-950 p-4">
          <ChatInput onSend={handleSendMessage} disabled={mutation.isPending} />
        </div>
      </div>
    </>
  );
}