package com.jansetu.backend.profile.dto;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.profile.fact.ProfileFactConsentScope;
import com.jansetu.backend.profile.fact.ProfileFactProvenance;
import com.jansetu.backend.profile.fact.ProfileFactVerificationStatus;

import java.time.Instant;

public record ProfileFactResponse(
        FactKey factKey,
        String value,
        ProfileFactProvenance provenance,
        ProfileFactVerificationStatus verificationStatus,
        ProfileFactConsentScope consentScope,
        Instant expiresAt,
        Instant capturedAt,
        boolean active
) {
    /** Citizen-facing DTO factory, avoiding accidental entity or internal ID leakage. */
    public static ProfileFactResponse from(com.jansetu.backend.profile.fact.CitizenProfileFact fact, boolean active) {
        return new ProfileFactResponse(fact.getFactKey(), fact.getValueText(), fact.getProvenance(),
                fact.getVerificationStatus(), fact.getConsentScope(), fact.getExpiresAt(),
                fact.getCapturedAt(), active);
    }
}
