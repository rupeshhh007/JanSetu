package com.jansetu.backend.source.api;

import com.jansetu.backend.policy.domain.PolicyRule;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SourceCandidatePolicyDraftRequest(
        @NotNull(message = "Policy effective date is required") LocalDate effectiveFrom,
        @NotNull(message = "A policy rule is required") PolicyRule policyRule
) { }
