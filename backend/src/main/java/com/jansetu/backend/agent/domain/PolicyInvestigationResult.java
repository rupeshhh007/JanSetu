package com.jansetu.backend.agent.domain;

import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.validation.PolicyValidationIssue;

import java.util.List;
import java.util.UUID;

public record PolicyInvestigationResult(
        UUID investigationId,
        AgentRunStatus status,
        UUID sourceCandidateId,
        UUID schemeId,
        List<UUID> snapshotIds,
        UUID previousPolicyVersionId,
        String proposedChangeSummary,
        PolicyRule proposedPolicyRule,
        List<EvidenceCitation> evidenceCitations,
        List<String> unverifiedClaims,
        List<PolicyValidationIssue> validationIssues,
        boolean reviewRequired,
        String recommendedNextAction,
        boolean deterministicFixture
) {
    public PolicyInvestigationResult {
        snapshotIds = List.copyOf(snapshotIds == null ? List.of() : snapshotIds);
        evidenceCitations = List.copyOf(evidenceCitations == null ? List.of() : evidenceCitations);
        unverifiedClaims = List.copyOf(unverifiedClaims == null ? List.of() : unverifiedClaims);
        validationIssues = List.copyOf(validationIssues == null ? List.of() : validationIssues);
    }
}
