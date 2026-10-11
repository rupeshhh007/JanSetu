package com.jansetu.backend.policy.evaluation;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyResult;

import java.util.Set;

/** Result plus all facts and rule evidence needed for an explainable outcome. */
public record PolicyEvaluation(
        PolicyResult result,
        PolicyEvaluationTrace trace,
        Set<FactKey> evaluatedFacts,
        Set<FactKey> missingFacts
) {
    public PolicyEvaluation {
        evaluatedFacts = evaluatedFacts == null ? Set.of() : Set.copyOf(evaluatedFacts);
        missingFacts = missingFacts == null ? Set.of() : Set.copyOf(missingFacts);
    }
}
