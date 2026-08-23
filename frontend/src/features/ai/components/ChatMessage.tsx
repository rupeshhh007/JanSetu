"use client";

import AIAvatar from "./AIAvatar";

import MessageBubble from "./MessageBubble";

import MarkdownMessage from "./MarkdownMessage";

import RecommendationCard from "@/features/ai/components/RecommendationCard";

import ComparisonCard from "@/features/ai/components/ComparisonCard";

import ApplicationStatusCard from "@/features/ai/components/ApplicationStatusCard";

import { detectResponseType } from "../utils/detectResponseType";

import type { ChatMessage } from "../types";

interface Props {

  message: ChatMessage;

}

export function ChatMessage({

  message,

}: Props) {

  const type =
    detectResponseType(message.content);

  if (message.role === "user") {
    return (
      <div className="flex w-full">
        <MessageBubble role="user">
          {message.content}
        </MessageBubble>
      </div>
    );
  }

  return (

    <div className="flex gap-4 items-start">

      <AIAvatar />

      <div className="flex-1">

        <MessageBubble role="assistant">

          {type === "recommendation" && (

            <RecommendationCard
              content={message.content}
            />

          )}

          {type === "comparison" && (

            <ComparisonCard
              content={message.content}
            />

          )}

          {type === "status" && (

            <ApplicationStatusCard
              content={message.content}
            />

          )}

          {type === "markdown" && (

            <MarkdownMessage
              content={message.content}
            />

          )}

        </MessageBubble>

      </div>

    </div>

  );

}