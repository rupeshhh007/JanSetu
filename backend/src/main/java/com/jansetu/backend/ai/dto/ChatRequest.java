package com.jansetu.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ChatRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 1_000, message = "Message must be at most 1000 characters") String message,
        @Size(max = 5, message = "At most five contextual schemes are allowed") List<@NotNull UUID> contextSchemeIds
) {
    public ChatRequest(String message) { this(message, List.of()); }
}
