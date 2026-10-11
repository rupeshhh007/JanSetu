package com.jansetu.backend.scheme.evaluator;

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
        int evaluatedRuleCount
) {
    public boolean isEligible() {
        return outcome == EligibilityOutcome.ELIGIBLE;
    }
}
