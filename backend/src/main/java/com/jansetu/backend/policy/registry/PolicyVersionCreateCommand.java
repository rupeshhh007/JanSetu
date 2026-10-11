package com.jansetu.backend.policy.registry;

import com.jansetu.backend.policy.domain.PolicyRule;

import java.time.LocalDate;
import java.util.List;

public record PolicyVersionCreateCommand(
        LocalDate effectiveFrom,
        PolicyRule policyRule,
        List<PolicySourceReference> sourceReferences
) {
    public PolicyVersionCreateCommand {
        sourceReferences = sourceReferences == null ? List.of() : List.copyOf(sourceReferences);
    }
}
