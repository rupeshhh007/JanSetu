package com.jansetu.backend.notification.api;

import com.jansetu.backend.notification.CitizenPolicyNotification;
import com.jansetu.backend.policy.domain.PolicyResult;

import java.time.Instant;
import java.util.UUID;

public record CitizenPolicyNotificationResponse(
        UUID id,
        UUID schemeId,
        String schemeName,
        UUID policyVersionId,
        int policyVersionNumber,
        PolicyResult previousResult,
        PolicyResult currentResult,
        String title,
        String message,
        Instant readAt,
        Instant createdAt
) {
    public static CitizenPolicyNotificationResponse from(CitizenPolicyNotification notification) {
        return new CitizenPolicyNotificationResponse(
                notification.getId(), notification.getScheme().getId(), notification.getScheme().getName(),
                notification.getPolicyVersion().getId(), notification.getPolicyVersion().getVersionNumber(),
                notification.getPreviousResult(), notification.getCurrentResult(), notification.getTitle(),
                notification.getMessage(), notification.getReadAt(), notification.getCreatedAt());
    }
}
