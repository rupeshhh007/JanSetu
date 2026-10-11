package com.jansetu.backend.policy.domain;

import java.math.BigDecimal;

public record NumericRangeRule(String id, String label, FactKey fact, BigDecimal minimum,
                               BigDecimal maximum) implements PolicyRule { }
