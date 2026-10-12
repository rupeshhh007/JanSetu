package com.jansetu.backend.policy.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PolicyTransitionRequest(
        @NotBlank(message = "An administrative reason is required")
        @Size(max = 1000) String reason
) { }
