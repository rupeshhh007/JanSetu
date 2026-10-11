package com.jansetu.backend.ai.dto;

import java.util.List;

public record ChatResponse(
        String response,
        List<Citation> citations,
        List<ChatCard> cards,
        List<String> followUpQuestions,
        boolean fallback
) {
    /** Preserves the pre-structured API contract for existing callers. */
    public ChatResponse(String response) {
        this(response, List.of(), List.of(), List.of(), true);
    }

    public static ChatResponse from(ChatResult result) {
        return new ChatResponse(
                result.response(),
                result.citations(),
                result.cards(),
                result.followUpQuestions(),
                result.fallback()
        );
    }
}
