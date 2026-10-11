package com.jansetu.backend.policy.domain;

import java.util.Set;

public record SetMembershipRule(String id, String label, FactKey fact, MembershipMode mode,
                                Set<String> values) implements PolicyRule {
    public SetMembershipRule {
        values = values == null ? Set.of() : Set.copyOf(values);
    }
}
