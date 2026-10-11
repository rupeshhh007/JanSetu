package com.jansetu.backend.policy.domain;

import java.util.List;

public record AnyOfRule(String id, String label, List<PolicyRule> children) implements PolicyRule {
    public AnyOfRule {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
