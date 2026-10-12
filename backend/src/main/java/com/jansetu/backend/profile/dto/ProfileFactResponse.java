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
) { }
