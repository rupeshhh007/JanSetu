package com.jansetu.backend.policy.domain;

import java.time.LocalDate;

public record DateRangeRule(String id, String label, FactKey fact, LocalDate earliest,
                            LocalDate latest) implements PolicyRule { }
