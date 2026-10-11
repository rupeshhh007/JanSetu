package com.jansetu.backend.policy.domain;

/** A typed, value-preserving fact captured with a historical assessment. */
public record PolicyFactSnapshot(FactKey fact, String value) { }
