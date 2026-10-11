package com.jansetu.backend.policy.evaluation;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyResult;

import java.util.List;
import java.util.Set;

/** A structured, data-only explanation produced by the evaluator itself. */
public record PolicyEvaluationTrace(
        String ruleId,
        String label,
        PolicyResult result,
        String reasonCode,
        Set<FactKey> evaluatedFacts,
        Set<FactKey> missingFacts,
        List<PolicyEvaluationTrace> children
) {
    public PolicyEvaluationTrace {
        evaluatedFacts = evaluatedFacts == null ? Set.of() : Set.copyOf(evaluatedFacts);
        missingFacts = missingFacts == null ? Set.of() : Set.copyOf(missingFacts);
        children = children == null ? List.of() : List.copyOf(children);
    }
}
