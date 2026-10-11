package com.jansetu.backend.policy.domain;

import java.util.List;

public record AllOfRule(String id, String label, List<PolicyRule> children) implements PolicyRule {
    public AllOfRule {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
