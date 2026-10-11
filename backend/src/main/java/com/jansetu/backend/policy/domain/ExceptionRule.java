package com.jansetu.backend.policy.domain;

/** A documented exception that exempts a citizen from a base requirement. */
public record ExceptionRule(String id, String label, PolicyRule baseRequirement,
                            PolicyRule exemptionCondition) implements PolicyRule { }
