package com.jansetu.backend.source.change;

/** A source change is never applied to a JanSetu policy until an administrator reviews it. */
public enum SourceChangeCandidateStatus {
    PENDING_REVIEW,
    APPLICABLE,
    REJECTED
}
