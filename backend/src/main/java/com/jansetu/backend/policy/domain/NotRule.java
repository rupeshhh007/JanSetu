package com.jansetu.backend.policy.domain;

public record NotRule(String id, String label, PolicyRule child) implements PolicyRule { }
