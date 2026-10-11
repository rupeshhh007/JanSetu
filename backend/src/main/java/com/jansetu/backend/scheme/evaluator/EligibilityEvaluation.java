package com.jansetu.backend.scheme.evaluator;

import com.jansetu.backend.policy.evaluation.PolicyEvaluationTrace;

import java.util.List;

/**
 * Explainable result of evaluating a profile against the recorded scheme rules.
 * The lists are safe to show to a citizen; they contain no hidden model logic.
 */
public record EligibilityEvaluation(
        EligibilityOutcome outcome,
        List<String> matchedCriteria,
        List<String> unmetCriteria,
        List<String> missingInformation,
        int evaluatedRuleCount,
        PolicyEvaluationTrace trace
) {
    public EligibilityEvaluation(EligibilityOutcome outcome, List<String> matchedCriteria,
                                 List<String> unmetCriteria, List<String> missingInformation,
                                 int evaluatedRuleCount) {
        this(outcome, matchedCriteria, unmetCriteria, missingInformation, evaluatedRuleCount, null);
    }

    public boolean isEligible() {
        return outcome == EligibilityOutcome.ELIGIBLE;
    }
}
