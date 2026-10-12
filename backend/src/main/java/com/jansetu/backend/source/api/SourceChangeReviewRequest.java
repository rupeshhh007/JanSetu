package com.jansetu.backend.source.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SourceChangeReviewRequest(
        UUID schemeId,
        @NotBlank(message = "A review reason is required.") @Size(max = 1000) String reason
) { }
