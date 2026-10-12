package com.jansetu.backend.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** A citizen can only submit a self-declared fact and explicitly choose its reuse scope. */
public record ProfileFactRequest(
        @NotBlank(message = "A fact value is required")
        @Size(max = 500, message = "A fact value must be at most 500 characters") String value,
        boolean consentToCaseworkerReuse,
        Instant expiresAt
) { }
