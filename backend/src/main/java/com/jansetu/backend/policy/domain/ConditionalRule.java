package com.jansetu.backend.policy.domain;

/** A requirement that applies only when its condition is satisfied. */
public record ConditionalRule(String id, String label, PolicyRule condition, PolicyRule whenTrue,
                              PolicyRule whenFalse) implements PolicyRule { }
