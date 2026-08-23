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
          fixed bottom-8 right-8 z-40 flex h-16 w-16 items-center justify-center 
          rounded-full bg-primary text-primary-foreground shadow-card border border-white/10 
          transition-all duration-300 ease-out hover:scale-105 hover:bg-primary-hover
          ${isOpen ? "scale-0 opacity-0 pointer-events-none" : "scale-100 opacity-100"}
        `}
      >
        <Sparkles size={28} className="animate-pulse" />
      </button>

      {/* Backdrop Overlay */}
      <div
        aria-hidden="true"
        onClick={() => setIsOpen(false)}
        className={`
          fixed inset-0 z-50 bg-foreground/10 backdrop-blur-md transition-opacity duration-500
          ${isOpen ? "opacity-100" : "pointer-events-none opacity-0"}
        `}
      />

      {/* Gemini-Style Centered Chat Modal */}
      <div
        id="ai-copilot-panel"
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        aria-label="JanSetu AI Assistant"
        className={`
          fixed inset-0 sm:inset-6 md:inset-x-[10%] lg:inset-x-[20%] xl:inset-x-[25%] md:inset-y-10 z-50 
          flex flex-col bg-background sm:rounded-[2rem] shadow-2xl overflow-hidden
          transition-all duration-500 ease-[cubic-bezier(0.32,0.72,0,1)] border border-black/5
          ${isOpen ? "translate-y-0 opacity-100 scale-100" : "translate-y-12 opacity-0 scale-95 pointer-events-none"}
        `}
      >
        {/* Header */}
        <header className="flex-shrink-0 flex items-center justify-between px-8 py-5">
          <div className="flex items-center gap-3">
            <Sparkles size={22} className="text-primary" />
            <h2 className="text-[18px] font-semibold text-foreground tracking-tight font-heading">
              JanSetu Copilot
            </h2>
          </div>
          <button
            onClick={() => setIsOpen(false)}
            aria-label="Close panel"
            className="rounded-full p-2 text-muted-foreground transition-colors hover:bg-black/5 hover:text-foreground"
          >
            <X size={24} />
          </button>
        </header>

        {/* Scrollable Body */}
        <div className="flex-1 overflow-y-auto px-4 sm:px-8 py-4 scroll-smooth">
          <div className="max-w-3xl mx-auto h-full flex flex-col">
            {messages.length === 0 ? (
              <div className="flex flex-1 flex-col justify-center pb-12 animate-in fade-in duration-700">
                <EmptyState onPromptClick={handleSendMessage} />
              </div>
            ) : (
              <div className="flex flex-col gap-8 pb-4">
                {messages.map((msg) => (
                  <div key={msg.id} className="animate-in fade-in slide-in-from-bottom-2 duration-300">
                    <ChatMessageComponent message={msg} />
                  </div>
                ))}
                {mutation.isPending && (
                  <div className="animate-in fade-in duration-300">
                    <TypingIndicator />
                  </div>
                )}
                <div ref={messagesEndRef} className="h-4 w-full" />
              </div>
            )}
          </div>
        </div>

        {/* Footer Input Area */}
        <div className="flex-shrink-0 px-4 sm:px-8 pb-6 pt-2 bg-gradient-to-t from-background via-background to-transparent">
          <div className="max-w-3xl mx-auto">
            <ChatInput onSend={handleSendMessage} disabled={mutation.isPending} />
            <p className="text-center text-[12px] text-muted-foreground mt-3 font-medium">
              Gemini may display inaccurate info, so double-check its responses.
            </p>
          </div>
        </div>
      </div>
    </>
  );
}