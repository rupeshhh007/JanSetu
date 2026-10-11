package com.jansetu.backend.policy.domain;

public record TextComparisonRule(String id, String label, FactKey fact, TextOperator operator,
                                 String expectedValue) implements PolicyRule { }
