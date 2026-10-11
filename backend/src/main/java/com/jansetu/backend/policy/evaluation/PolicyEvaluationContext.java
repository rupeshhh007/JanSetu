package com.jansetu.backend.policy.evaluation;

import com.jansetu.backend.policy.domain.PolicyFacts;

import java.time.LocalDate;
import java.util.Objects;

/** Inputs that make an evaluation reproducible at any later time. */
public record PolicyEvaluationContext(PolicyFacts facts, LocalDate evaluationDate) {
    public PolicyEvaluationContext {
        Objects.requireNonNull(facts, "facts are required");
        Objects.requireNonNull(evaluationDate, "evaluation date is required");
    }
}
