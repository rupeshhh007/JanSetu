"use client";
import Link from "next/link";
import { ExternalLink, ArrowRight, BookOpen } from "lucide-react";
import MarkdownMessage from "./MarkdownMessage";
import type { ChatMessage as Message } from "../types";
export function ChatMessage({
  message,
  onFollowUp,
  disabled,
}: {
  message: Message;
  onFollowUp?: (question: string) => void;
  disabled?: boolean;
}) {
  if (message.role === "user")
    return (
      <div className="ml-10 rounded-xl rounded-br-sm bg-accent px-4 py-3 text-sm leading-6">
        {message.content}
      </div>
    );
  return (
    <div className="min-w-0">
      <div className="mb-3 flex items-center gap-2 text-xs font-semibold text-primary">
        <BookOpen size={15} />
        JanSetu Guide
      </div>
      <MarkdownMessage content={message.content} />
      {!!message.cards?.length && (
        <div className="mt-4 space-y-2">
          {message.cards
            .filter((card) => card.schemeId)
            .map((card) => (
              <Link
                key={card.schemeId}
                href={`/schemes/${card.schemeId}`}
                onClick={(e) => e.currentTarget.closest("dialog")?.close()}
                className="flex items-center justify-between gap-4 rounded-lg border bg-muted/30 p-3 text-xs font-medium hover:bg-muted"
              >
                <span>
                  {card.title}
                  <span className="mt-1 block font-normal text-muted-foreground">
                    View details & prepare documents
                  </span>
                </span>
                <ArrowRight size={15} className="shrink-0 text-primary" />
              </Link>
            ))}
        </div>
      )}
      {!!message.citations?.length && (
        <details className="mt-4 rounded-lg border p-3 text-xs">
          <summary className="cursor-pointer font-medium text-primary">
            {message.citations.length} catalogue source
            {message.citations.length === 1 ? "" : "s"}
          </summary>
          <ul className="mt-3 space-y-3">
            {message.citations.map((citation) => (
              <li key={`${citation.title}-${citation.url}`}>
                <a
                  href={citation.url ?? undefined}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-1 text-primary underline"
                >
                  {citation.title}
                  <ExternalLink size={11} />
                </a>
                <p className="mt-1 text-muted-foreground">
                  {citation.sourceName}
                  {citation.lastVerifiedAt
                    ? ` · Recorded ${new Date(citation.lastVerifiedAt).toLocaleDateString("en-IN")}`
                    : ""}
                </p>
              </li>
            ))}
          </ul>
        </details>
      )}
      {message.fallback && (
        <p className="mt-3 text-[10px] text-muted-foreground">
          Catalogue answer · no external model used
        </p>
      )}
      {!!message.followUpQuestions?.length && (
        <div className="mt-3 flex flex-wrap gap-2">
          {message.followUpQuestions.map((question) => (
            <button
              key={question}
              disabled={disabled}
              onClick={() => onFollowUp?.(question)}
              className="rounded-lg border px-3 py-2 text-left text-xs text-primary hover:bg-accent disabled:opacity-50"
            >
              {question}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
