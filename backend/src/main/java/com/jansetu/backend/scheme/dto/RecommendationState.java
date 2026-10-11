package com.jansetu.backend.scheme.dto;

/** Citizen-facing recommendation state derived from deterministic policy evidence. */
public enum RecommendationState {
    READY_TO_REVIEW,
    PROFILE_INFORMATION_NEEDED,
    POLICY_INFORMATION_UNAVAILABLE
}
