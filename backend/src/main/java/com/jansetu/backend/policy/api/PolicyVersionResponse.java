package com.jansetu.backend.policy.api;

import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PolicyVersionResponse(
        UUID id,
        UUID schemeId,
        int versionNumber,
        PolicyVersionStatus status,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Instant recordedAt,
        Instant approvedAt,
        Instant publishedAt,
        Instant supersededAt,
        PolicyRule policyRule,
        List<PolicySourceReference> sourceReferences,
        long revision
) {
    public static PolicyVersionResponse from(PolicyVersion version) {
        return new PolicyVersionResponse(version.getId(), version.getScheme().getId(), version.getVersionNumber(),
                version.getStatus(), version.getEffectiveFrom(), version.getEffectiveTo(), version.getRecordedAt(),
                version.getApprovedAt(), version.getPublishedAt(), version.getSupersededAt(), version.getPolicyRule(),
                version.getSourceReferences(), version.getRevision());
    }
}
