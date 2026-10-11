package com.jansetu.backend.policy.domain;

/** Age is calculated using the evaluation date supplied by the caller. */
public record AgeRangeRule(String id, String label, Integer minimumAge, Integer maximumAge) implements PolicyRule { }
