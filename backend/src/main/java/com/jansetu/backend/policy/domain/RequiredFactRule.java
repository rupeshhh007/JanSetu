package com.jansetu.backend.policy.domain;

public record RequiredFactRule(String id, String label, FactKey fact) implements PolicyRule { }
