package com.jansetu.backend.policy.domain;

public record BooleanMatchRule(String id, String label, FactKey fact, boolean expectedValue) implements PolicyRule { }
