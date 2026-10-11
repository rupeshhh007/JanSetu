package com.jansetu.backend.ai.dto;

import java.util.List;

/** Internal, structured response before it is exposed through the API DTO. */
public record ChatResult(
        String response,
        List<Citation> citations,
        List<ChatCard> cards,
        List<String> followUpQuestions,
        boolean fallback
) {
    public static ChatResult text(String response) {
        return new ChatResult(response, List.of(), List.of(), List.of(), true);
    }
}
