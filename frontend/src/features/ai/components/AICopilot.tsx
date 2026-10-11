"use client";
import { useState, useRef, useEffect, useCallback } from "react";
import {
  X,
  MessageCircle,
  RotateCcw,
  SendHorizontal,
  ArrowUpRight,
  BookOpen,
} from "lucide-react";
import { useAIChat } from "../hooks/useAIChat";
import { ChatMessage as Message } from "./ChatMessage";
import type { ChatMessage } from "../types";
const PROMPTS = [
  "Which schemes should I apply for?",
  "Compare PM-KISAN and Ayushman Bharat PM-JAY",
  "What is my application status?",
  "What documents do I need for PM-KISAN?",
];
export function AICopilot() {
  const dialog = useRef<HTMLDialogElement>(null);
  const end = useRef<HTMLDivElement>(null);
  const input = useRef<HTMLTextAreaElement>(null);
  const context = useRef<string[]>([]);
  const sending = useRef(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState("");
  const [failed, setFailed] = useState<string | null>(null);
  const { mutateAsync, isPending } = useAIChat();
  const send = useCallback(
    async (text: string, contextualIds?: string[]) => {
      const content = text.trim();
      if (!content || content.length > 1000 || sending.current) return;
      sending.current = true;
      setFailed(null);
      setDraft("");
      setMessages((previous) => [
        ...previous,
        { id: crypto.randomUUID(), role: "user", content },
      ]);
      try {
        const response = await mutateAsync({
          message: content,
          contextSchemeIds: contextualIds ?? context.current,
        });
        context.current = [
          ...new Set(
            response.cards
              .map((card) => card.schemeId)
              .filter((id): id is string => !!id),
          ),
        ].slice(0, 5);
        setMessages((previous) => [
          ...previous,
          {
            id: crypto.randomUUID(),
            role: "assistant",
            content: response.response,
            ...response,
          },
        ]);
      } catch {
        setFailed(content);
      } finally {
        sending.current = false;
        requestAnimationFrame(() => input.current?.focus());
      }
    },
    [mutateAsync],
  );
  useEffect(() => {
    const open = (event: Event) => {
      if (!dialog.current?.open) dialog.current?.showModal();
      const detail = (
        event as CustomEvent<{ message?: string; schemeId?: string }>
      ).detail;
      if (detail?.message)
        void send(
          detail.message,
          detail.schemeId ? [detail.schemeId] : undefined,
        );
      else input.current?.focus();
    };
    window.addEventListener("jansetu:guide", open);
    return () => window.removeEventListener("jansetu:guide", open);
  }, [send]);
  useEffect(() => {
    if (dialog.current?.open)
      end.current?.scrollIntoView({ behavior: "instant", block: "end" });
  }, [messages, isPending, failed]);
  return (
    <>
      <button
        onClick={() => {
          dialog.current?.showModal();
          input.current?.focus();
        }}
        aria-label="Open JanSetu Guide"
        className="no-print fixed bottom-5 right-5 z-30 flex size-12 items-center justify-center rounded-full bg-primary text-white shadow-lg transition-colors hover:bg-primary-hover lg:hidden"
      >
        <MessageCircle size={23} />
      </button>
      <dialog
        ref={dialog}
        aria-labelledby="guide-title"
        onClick={(event) => {
          if (event.target === event.currentTarget) dialog.current?.close();
        }}
        className="fixed inset-y-0 left-auto right-0 m-0 h-dvh max-h-none w-full max-w-[620px] border-l bg-white p-0 text-foreground shadow-2xl backdrop:bg-[#0b211b]/35 open:flex open:flex-col sm:inset-y-4 sm:right-4 sm:h-[calc(100dvh-2rem)] sm:rounded-2xl sm:border"
      >
        <header className="flex shrink-0 items-center justify-between gap-3 border-b p-5">
          <div className="flex items-center gap-3">
            <span className="flex size-10 items-center justify-center rounded-xl bg-accent text-primary">
              <MessageCircle size={21} />
            </span>
            <div>
              <h2 id="guide-title" className="text-base">
                JanSetu Guide
              </h2>
              <p className="text-xs text-muted-foreground">
                Your scheme research companion
              </p>
            </div>
          </div>
          <div className="flex gap-1">
            <button
              disabled={isPending || !messages.length}
              aria-label="Clear conversation"
              onClick={() => {
                setMessages([]);
                context.current = [];
                setFailed(null);
              }}
              className="rounded-lg p-2 text-muted-foreground hover:bg-muted disabled:opacity-30"
            >
              <RotateCcw size={18} />
            </button>
            <button
              aria-label="Close guide"
              onClick={() => dialog.current?.close()}
              className="rounded-lg p-2 hover:bg-muted"
            >
              <X size={21} />
            </button>
          </div>
        </header>
        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain px-5 py-6 sm:px-7">
          {!messages.length ? (
            <div className="flex min-h-full flex-col justify-center">
              <BookOpen
                size={29}
                className="mb-5 text-primary"
                strokeWidth={1.5}
              />
              <h3 className="editorial text-4xl">Let’s find your next step.</h3>
              <p className="mt-4 max-w-sm text-sm leading-6 text-muted-foreground">
                Ask about benefits, compare your options, or understand the
                documents you’ll need. Answers are grounded in the JanSetu
                catalogue.
              </p>
              <div className="mt-7 space-y-3">
                {PROMPTS.map((prompt) => (
                  <button
                    key={prompt}
                    onClick={() => void send(prompt)}
                    className="flex w-full items-center justify-between gap-4 rounded-xl border p-4 text-left text-sm transition-colors hover:border-primary/40 hover:bg-muted/50"
                  >
                    {prompt}
                    <ArrowUpRight className="shrink-0 text-primary" size={17} />
                  </button>
                ))}
              </div>
              <p className="mt-6 text-xs leading-5 text-muted-foreground">
                Local catalogue mode works without a model API key.
                Conversations stay in this workspace session.
              </p>
            </div>
          ) : (
            <div
              className="space-y-7"
              role="log"
              aria-label="Conversation"
              aria-live="polite"
              aria-relevant="additions"
            >
              {messages.map((message) => (
                <Message
                  key={message.id}
                  message={message}
                  disabled={isPending}
                  onFollowUp={(text) => void send(text)}
                />
              ))}
              {isPending && (
                <p
                  role="status"
                  className="flex items-center gap-2 text-sm text-muted-foreground"
                >
                  <span className="size-2 animate-pulse rounded-full bg-primary" />
                  Checking the catalogue…
                </p>
              )}
              {failed && (
                <div
                  role="alert"
                  className="rounded-xl border border-destructive/25 bg-red-50 p-4 text-sm"
                >
                  <p>
                    We couldn’t reach the guide. Your question is still here.
                  </p>
                  <button
                    className="text-link mt-2"
                    onClick={() => void send(failed)}
                  >
                    Retry question
                  </button>
                </div>
              )}
              <div ref={end} />
            </div>
          )}
        </div>
        <form
          className="shrink-0 border-t p-4 sm:p-5"
          onSubmit={(event) => {
            event.preventDefault();
            void send(draft);
          }}
        >
          <div className="flex items-end gap-2 rounded-xl border bg-background p-2 focus-within:border-primary">
            <textarea
              ref={input}
              aria-label="Ask JanSetu Guide"
              rows={2}
              maxLength={1000}
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              placeholder="Ask a question, or follow up…"
              onKeyDown={(e) => {
                if (
                  e.key === "Enter" &&
                  !e.shiftKey &&
                  !e.nativeEvent.isComposing
                ) {
                  e.preventDefault();
                  void send(draft);
                }
              }}
              className="max-h-32 min-h-12 flex-1 resize-none bg-transparent p-2 text-sm outline-none"
            />
            <button
              type="submit"
              disabled={isPending || !draft.trim()}
              aria-label="Send message"
              className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary text-white disabled:opacity-40"
            >
              <SendHorizontal size={18} />
            </button>
          </div>
          <div className="mt-2 flex justify-between gap-3 text-[10px] text-muted-foreground">
            <span>
              Verify important requirements with the official department.
            </span>
            <span>{draft.length}/1000</span>
          </div>
        </form>
      </dialog>
    </>
  );
}
