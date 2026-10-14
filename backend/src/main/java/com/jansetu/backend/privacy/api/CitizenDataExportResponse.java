package com.jansetu.backend.privacy.api;

import com.jansetu.backend.application.dto.ApplicationResponse;
import com.jansetu.backend.assessment.api.PolicyAssessmentResponse;
import com.jansetu.backend.notification.api.CitizenPolicyNotificationResponse;
import com.jansetu.backend.preparation.api.ApplicationPreparationResponse;
import com.jansetu.backend.profile.dto.ProfileFactResponse;
import com.jansetu.backend.profile.dto.ProfileResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** An explicit set of citizen-safe DTOs, not raw Hibernate entities or credential hashes. */
public record CitizenDataExportResponse(
        String exportVersion,
        Instant generatedAt,
        ProfileResponse profile,
        List<ProfileFactResponse> recordedFacts,
        List<UUID> savedSchemeIds,
        List<ApplicationResponse> applications,
        List<ApplicationPreparationResponse> preparations,
        List<PolicyAssessmentResponse> assessments,
        List<CitizenPolicyNotificationResponse> notifications
) { }
