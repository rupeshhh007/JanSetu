package com.jansetu.backend.scheme.evaluator;

/**
 * A deterministic eligibility assessment.  We deliberately keep this separate
 * from a recommendation score: eligibility is a rules decision, not a model
 * prediction.
 */
public enum EligibilityOutcome {
    ELIGIBLE,
    NOT_ELIGIBLE,
    NEEDS_INFORMATION
}
