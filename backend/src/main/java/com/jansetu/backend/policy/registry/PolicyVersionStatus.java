package com.jansetu.backend.policy.registry;

/** Lifecycle state of an immutable policy version. */
public enum PolicyVersionStatus {
    DRAFT,
    IN_REVIEW,
    APPROVED,
    PUBLISHED,
    SUPERSEDED,
    ARCHIVED
}
