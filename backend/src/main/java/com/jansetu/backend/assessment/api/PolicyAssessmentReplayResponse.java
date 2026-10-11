package com.jansetu.backend.assessment.api;

import com.jansetu.backend.assessment.PolicyAssessmentReplay;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationTrace;

import java.util.UUID;

public record PolicyAssessmentReplayResponse(UUID assessmentId, PolicyResult recordedRuleResult,
                                             PolicyResult replayedRuleResult, boolean matchesRecordedResult,
                                             PolicyEvaluationTrace replayTrace) {
    public static PolicyAssessmentReplayResponse from(PolicyAssessmentReplay replay) {
        return new PolicyAssessmentReplayResponse(replay.assessmentId(), replay.recordedResult(), replay.replayedResult(),
                replay.matchesRecordedResult(), replay.replayTrace());
    }
}
