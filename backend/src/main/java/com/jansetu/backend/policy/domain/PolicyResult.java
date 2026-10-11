package com.jansetu.backend.policy.domain;

/** Four-valued result used by the deterministic policy engine. */
public enum PolicyResult {
    SATISFIED,
    FAILED,
    UNKNOWN,
    NOT_APPLICABLE
}
