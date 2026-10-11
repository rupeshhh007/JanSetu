package com.jansetu.backend.policy.api;

import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PolicyVersionRequest(
        @NotNull(message = "Policy effective date is required") LocalDate effectiveFrom,
        @NotNull(message = "A policy rule is required") PolicyRule policyRule,
        List<PolicySourceReference> sourceReferences
) {
    public PolicyVersionRequest {
        sourceReferences = sourceReferences == null ? List.of() : List.copyOf(sourceReferences);
    }
}
