package com.jansetu.backend.policy.evaluation;

import com.jansetu.backend.policy.domain.AgeRangeRule;
import com.jansetu.backend.policy.domain.AllOfRule;
import com.jansetu.backend.policy.domain.AnyOfRule;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.ConditionalRule;
import com.jansetu.backend.policy.domain.DateRangeRule;
import com.jansetu.backend.policy.domain.ExceptionRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.MembershipMode;
import com.jansetu.backend.policy.domain.NotRule;
import com.jansetu.backend.policy.domain.NumericComparisonRule;
import com.jansetu.backend.policy.domain.NumericOperator;
import com.jansetu.backend.policy.domain.NumericRangeRule;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.domain.RequiredFactRule;
import com.jansetu.backend.policy.domain.SetMembershipRule;
import com.jansetu.backend.policy.domain.TextComparisonRule;
import com.jansetu.backend.policy.domain.TextOperator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Evaluates only the published, typed policy representation.  It is pure:
 * outcomes depend solely on the supplied facts, policy tree, and evaluation
 * date, which makes historical replay deterministic.
 */
@Component
public class PolicyEvaluationEngine {

    public PolicyEvaluation evaluate(PolicyRule rule, PolicyEvaluationContext context) {
        if (rule == null) {
            PolicyEvaluationTrace trace = trace("unrecorded-policy", "Recorded policy", PolicyResult.UNKNOWN,
                    "POLICY_NOT_RECORDED", Set.of(), Set.of(), List.of());
            return new PolicyEvaluation(PolicyResult.UNKNOWN, trace, Set.of(), Set.of());
        }
        NodeEvaluation node = evaluateNode(rule, context);
        return new PolicyEvaluation(node.result(), node.trace(), node.evaluatedFacts(), node.missingFacts());
    }

    private NodeEvaluation evaluateNode(PolicyRule rule, PolicyEvaluationContext context) {
        if (rule instanceof AllOfRule allOf) {
            return logical(allOf.id(), allOf.label(), allOf.children(), context, true);
        }
        if (rule instanceof AnyOfRule anyOf) {
            return logical(anyOf.id(), anyOf.label(), anyOf.children(), context, false);
        }
        if (rule instanceof NotRule not) {
            NodeEvaluation child = evaluateNode(not.child(), context);
            PolicyResult result = switch (child.result()) {
                case SATISFIED -> PolicyResult.FAILED;
                case FAILED -> PolicyResult.SATISFIED;
                case UNKNOWN -> PolicyResult.UNKNOWN;
                case NOT_APPLICABLE -> PolicyResult.NOT_APPLICABLE;
            };
            return node(not.id(), not.label(), result, "LOGICAL_NOT", child.evaluatedFacts(), child.missingFacts(),
                    List.of(child.trace()));
        }
        if (rule instanceof RequiredFactRule requiredFact) {
            return requiredFact(requiredFact, context);
        }
        if (rule instanceof NumericComparisonRule comparison) {
            return numberComparison(comparison, context);
        }
        if (rule instanceof NumericRangeRule range) {
            return numberRange(range, context);
        }
        if (rule instanceof DateRangeRule range) {
            return dateRange(range, context);
        }
        if (rule instanceof AgeRangeRule range) {
            return ageRange(range, context);
        }
        if (rule instanceof TextComparisonRule comparison) {
            return textComparison(comparison, context);
        }
        if (rule instanceof SetMembershipRule membership) {
            return setMembership(membership, context);
        }
        if (rule instanceof BooleanMatchRule booleanRule) {
            return booleanMatch(booleanRule, context);
        }
        if (rule instanceof ConditionalRule conditional) {
            return conditional(conditional, context);
        }
        if (rule instanceof ExceptionRule exceptionRule) {
            return exception(exceptionRule, context);
        }
        throw new IllegalArgumentException("Unsupported policy rule: " + rule.getClass().getName());
    }

    private NodeEvaluation logical(String id, String label, List<PolicyRule> rules,
                                   PolicyEvaluationContext context, boolean isAnd) {
        List<NodeEvaluation> children = rules.stream().map(rule -> evaluateNode(rule, context)).toList();
        List<PolicyEvaluationTrace> traces = children.stream().map(NodeEvaluation::trace).toList();
        Set<FactKey> facts = facts(children);
        Set<FactKey> missing = missing(children);
        List<PolicyResult> results = children.stream().map(NodeEvaluation::result).toList();
        PolicyResult result = isAnd ? and(results) : or(results);
        return node(id, label, result, isAnd ? "LOGICAL_AND" : "LOGICAL_OR", facts, missing, traces);
    }

    private NodeEvaluation requiredFact(RequiredFactRule rule, PolicyEvaluationContext context) {
        if (context.facts().value(rule.fact()).isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        return node(rule.id(), rule.label(), PolicyResult.SATISFIED, "FACT_PRESENT", Set.of(rule.fact()), Set.of(), List.of());
    }

    private NodeEvaluation numberComparison(NumericComparisonRule rule, PolicyEvaluationContext context) {
        Optional<BigDecimal> value = context.facts().decimal(rule.fact());
        if (value.isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        int comparison = value.get().compareTo(rule.expectedValue());
        boolean satisfied = switch (rule.operator()) {
            case EQUALS -> comparison == 0;
            case NOT_EQUALS -> comparison != 0;
            case GREATER_THAN -> comparison > 0;
            case GREATER_THAN_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_THAN_OR_EQUAL -> comparison <= 0;
        };
        return leaf(rule.id(), rule.label(), satisfied, "NUMERIC_COMPARISON", rule.fact());
    }

    private NodeEvaluation numberRange(NumericRangeRule rule, PolicyEvaluationContext context) {
        Optional<BigDecimal> value = context.facts().decimal(rule.fact());
        if (value.isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        boolean aboveMinimum = rule.minimum() == null || value.get().compareTo(rule.minimum()) >= 0;
        boolean belowMaximum = rule.maximum() == null || value.get().compareTo(rule.maximum()) <= 0;
        return leaf(rule.id(), rule.label(), aboveMinimum && belowMaximum, "NUMERIC_RANGE", rule.fact());
    }

    private NodeEvaluation dateRange(DateRangeRule rule, PolicyEvaluationContext context) {
        Optional<LocalDate> value = context.facts().date(rule.fact());
        if (value.isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        boolean afterEarliest = rule.earliest() == null || !value.get().isBefore(rule.earliest());
        boolean beforeLatest = rule.latest() == null || !value.get().isAfter(rule.latest());
        return leaf(rule.id(), rule.label(), afterEarliest && beforeLatest, "DATE_RANGE", rule.fact());
    }

    private NodeEvaluation ageRange(AgeRangeRule rule, PolicyEvaluationContext context) {
        Optional<LocalDate> birthDate = context.facts().date(FactKey.BIRTH_DATE);
        if (birthDate.isEmpty()) {
            return missing(rule.id(), rule.label(), FactKey.BIRTH_DATE);
        }
        int age = Period.between(birthDate.get(), context.evaluationDate()).getYears();
        boolean aboveMinimum = rule.minimumAge() == null || age >= rule.minimumAge();
        boolean belowMaximum = rule.maximumAge() == null || age <= rule.maximumAge();
        return leaf(rule.id(), rule.label(), aboveMinimum && belowMaximum, "AGE_RANGE", FactKey.BIRTH_DATE);
    }

    private NodeEvaluation textComparison(TextComparisonRule rule, PolicyEvaluationContext context) {
        Optional<String> value = context.facts().text(rule.fact());
        if (value.isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        boolean equal = rule.expectedValue().equalsIgnoreCase(value.get());
        boolean satisfied = rule.operator() == TextOperator.EQUALS ? equal : !equal;
        return leaf(rule.id(), rule.label(), satisfied, "TEXT_COMPARISON", rule.fact());
    }

    private NodeEvaluation setMembership(SetMembershipRule rule, PolicyEvaluationContext context) {
        Optional<String> value = context.facts().text(rule.fact());
        if (value.isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        boolean included = rule.values().stream().anyMatch(candidate -> candidate.equalsIgnoreCase(value.get()));
        boolean satisfied = rule.mode() == MembershipMode.INCLUDED ? included : !included;
        return leaf(rule.id(), rule.label(), satisfied, "SET_MEMBERSHIP", rule.fact());
    }

    private NodeEvaluation booleanMatch(BooleanMatchRule rule, PolicyEvaluationContext context) {
        Optional<Boolean> value = context.facts().bool(rule.fact());
        if (value.isEmpty()) {
            return missing(rule.id(), rule.label(), rule.fact());
        }
        return leaf(rule.id(), rule.label(), value.get() == rule.expectedValue(), "BOOLEAN_MATCH", rule.fact());
    }

    private NodeEvaluation conditional(ConditionalRule rule, PolicyEvaluationContext context) {
        NodeEvaluation condition = evaluateNode(rule.condition(), context);
        if (condition.result() == PolicyResult.SATISFIED) {
            NodeEvaluation selected = evaluateNode(rule.whenTrue(), context);
            return node(rule.id(), rule.label(), selected.result(), "CONDITION_TRUE", selected.evaluatedFacts(),
                    selected.missingFacts(), List.of(condition.trace(), selected.trace()));
        }
        if (condition.result() == PolicyResult.FAILED && rule.whenFalse() != null) {
            NodeEvaluation selected = evaluateNode(rule.whenFalse(), context);
            return node(rule.id(), rule.label(), selected.result(), "CONDITION_FALSE", selected.evaluatedFacts(),
                    selected.missingFacts(), List.of(condition.trace(), selected.trace()));
        }
        if (condition.result() == PolicyResult.FAILED || condition.result() == PolicyResult.NOT_APPLICABLE) {
            return node(rule.id(), rule.label(), PolicyResult.NOT_APPLICABLE, "CONDITION_NOT_APPLICABLE",
                    condition.evaluatedFacts(), condition.missingFacts(), List.of(condition.trace()));
        }
        return node(rule.id(), rule.label(), PolicyResult.UNKNOWN, "CONDITION_UNKNOWN",
                condition.evaluatedFacts(), condition.missingFacts(), List.of(condition.trace()));
    }

    private NodeEvaluation exception(ExceptionRule rule, PolicyEvaluationContext context) {
        NodeEvaluation exemption = evaluateNode(rule.exemptionCondition(), context);
        if (exemption.result() == PolicyResult.SATISFIED) {
            return node(rule.id(), rule.label(), PolicyResult.NOT_APPLICABLE, "EXCEPTION_APPLIES",
                    exemption.evaluatedFacts(), exemption.missingFacts(), List.of(exemption.trace()));
        }
        NodeEvaluation base = evaluateNode(rule.baseRequirement(), context);
        Set<FactKey> facts = union(exemption.evaluatedFacts(), base.evaluatedFacts());
        Set<FactKey> missing = union(exemption.missingFacts(), base.missingFacts());
        PolicyResult result = switch (base.result()) {
            case SATISFIED -> PolicyResult.SATISFIED;
            case FAILED, UNKNOWN -> exemption.result() == PolicyResult.UNKNOWN ? PolicyResult.UNKNOWN : base.result();
            case NOT_APPLICABLE -> PolicyResult.NOT_APPLICABLE;
        };
        return node(rule.id(), rule.label(), result, "EXCEPTION_NOT_PROVEN", facts, missing,
                List.of(exemption.trace(), base.trace()));
    }

    private PolicyResult and(List<PolicyResult> results) {
        if (results.stream().anyMatch(result -> result == PolicyResult.FAILED)) {
            return PolicyResult.FAILED;
        }
        if (results.stream().anyMatch(result -> result == PolicyResult.UNKNOWN)) {
            return PolicyResult.UNKNOWN;
        }
        if (results.stream().anyMatch(result -> result == PolicyResult.SATISFIED)) {
            return PolicyResult.SATISFIED;
        }
        return PolicyResult.NOT_APPLICABLE;
    }

    private PolicyResult or(List<PolicyResult> results) {
        if (results.stream().anyMatch(result -> result == PolicyResult.SATISFIED)) {
            return PolicyResult.SATISFIED;
        }
        if (results.stream().anyMatch(result -> result == PolicyResult.UNKNOWN)) {
            return PolicyResult.UNKNOWN;
        }
        if (results.stream().anyMatch(result -> result == PolicyResult.FAILED)) {
            return PolicyResult.FAILED;
        }
        return PolicyResult.NOT_APPLICABLE;
    }

    private NodeEvaluation leaf(String id, String label, boolean satisfied, String reasonCode, FactKey fact) {
        return node(id, label, satisfied ? PolicyResult.SATISFIED : PolicyResult.FAILED, reasonCode,
                Set.of(fact), Set.of(), List.of());
    }

    private NodeEvaluation missing(String id, String label, FactKey fact) {
        return node(id, label, PolicyResult.UNKNOWN, "FACT_MISSING", Set.of(), Set.of(fact), List.of());
    }

    private NodeEvaluation node(String id, String label, PolicyResult result, String reasonCode,
                                Set<FactKey> facts, Set<FactKey> missing, List<PolicyEvaluationTrace> children) {
        return new NodeEvaluation(result, trace(id, label, result, reasonCode, facts, missing, children), facts, missing);
    }

    private PolicyEvaluationTrace trace(String id, String label, PolicyResult result, String reasonCode,
                                        Set<FactKey> facts, Set<FactKey> missing,
                                        List<PolicyEvaluationTrace> children) {
        return new PolicyEvaluationTrace(id, label, result, reasonCode, facts, missing, children);
    }

    private Set<FactKey> facts(List<NodeEvaluation> children) {
        return children.stream().map(NodeEvaluation::evaluatedFacts)
                .collect(LinkedHashSet::new, Set::addAll, Set::addAll);
    }

    private Set<FactKey> missing(List<NodeEvaluation> children) {
        return children.stream().map(NodeEvaluation::missingFacts)
                .collect(LinkedHashSet::new, Set::addAll, Set::addAll);
    }

    private Set<FactKey> union(Set<FactKey> first, Set<FactKey> second) {
        Set<FactKey> values = new LinkedHashSet<>(first);
        values.addAll(second);
        return values;
    }

    private record NodeEvaluation(PolicyResult result, PolicyEvaluationTrace trace,
                                  Set<FactKey> evaluatedFacts, Set<FactKey> missingFacts) { }
}
