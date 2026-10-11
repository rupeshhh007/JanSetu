package com.jansetu.backend.assessment.api;

import com.jansetu.backend.assessment.PolicyAssessment;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationTrace;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Citizen-safe assessment representation: it exposes trace keys, not raw fact values. */
public record PolicyAssessmentResponse(
        UUID id,
        UUID schemeId,
        String schemeName,
        UUID policyVersionId,
        int policyVersionNumber,
        LocalDate evaluationDate,
        Instant assessedAt,
        PolicyResult recordedRuleResult,
        PolicyEvaluationTrace trace,
        String officialDecisionNotice
) {
    public static PolicyAssessmentResponse from(PolicyAssessment assessment) {
        return new PolicyAssessmentResponse(assessment.getId(), assessment.getPolicyVersion().getScheme().getId(),
                assessment.getPolicyVersion().getScheme().getName(), assessment.getPolicyVersion().getId(),
                assessment.getPolicyVersion().getVersionNumber(), assessment.getEvaluationDate(), assessment.getAssessedAt(),
                assessment.getPolicyResult(), assessment.getEvaluationTrace(),
                "This is a recorded-rule assessment, not an official eligibility decision or government application result.");
    }
}
