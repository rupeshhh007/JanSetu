package com.jansetu.backend.policy.domain;

import java.math.BigDecimal;

public record NumericComparisonRule(String id, String label, FactKey fact, NumericOperator operator,
                                    BigDecimal expectedValue) implements PolicyRule { }
