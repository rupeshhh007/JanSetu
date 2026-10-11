package com.jansetu.backend.assessment;

import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationTrace;

import java.util.UUID;

/** Replays the exact historical inputs and makes divergence visible instead of overwriting history. */
public record PolicyAssessmentReplay(
        UUID assessmentId,
        PolicyResult recordedResult,
        PolicyResult replayedResult,
        boolean matchesRecordedResult,
        PolicyEvaluationTrace replayTrace
) { }
