package com.jansetu.backend.policy.evaluation;

import com.jansetu.backend.policy.domain.AgeRangeRule;
import com.jansetu.backend.policy.domain.AllOfRule;
import com.jansetu.backend.policy.domain.AnyOfRule;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.ConditionalRule;
import com.jansetu.backend.policy.domain.ExceptionRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.NumericComparisonRule;
import com.jansetu.backend.policy.domain.NumericOperator;
import com.jansetu.backend.policy.domain.NotRule;
import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.domain.SetMembershipRule;
import com.jansetu.backend.policy.domain.MembershipMode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyEvaluationEngineTest {
    private final PolicyEvaluationEngine engine = new PolicyEvaluationEngine();
    private final LocalDate evaluationDate = LocalDate.of(2026, 10, 11);

    @Test
    void failedAndUnknownIsFailed() {
        PolicyEvaluation evaluation = evaluate(new AllOfRule("root", "All recorded conditions", List.of(
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true),
                incomeUnder("income", "Income limit", "250000")
        )), PolicyFacts.builder().put(FactKey.IS_STUDENT, false).build());

        assertEquals(PolicyResult.FAILED, evaluation.result());
        assertEquals(Set.of(FactKey.IS_STUDENT), evaluation.evaluatedFacts());
        assertEquals(Set.of(FactKey.ANNUAL_FAMILY_INCOME), evaluation.missingFacts());
    }

    @Test
    void satisfiedAndUnknownIsUnknown() {
        PolicyEvaluation evaluation = evaluate(new AllOfRule("root", "All recorded conditions", List.of(
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true),
                incomeUnder("income", "Income limit", "250000")
        )), PolicyFacts.builder().put(FactKey.IS_STUDENT, true).build());

        assertEquals(PolicyResult.UNKNOWN, evaluation.result());
    }

    @Test
    void satisfiedOrUnknownIsSatisfied() {
        PolicyEvaluation evaluation = evaluate(new AnyOfRule("root", "Any recorded condition", List.of(
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true),
                incomeUnder("income", "Income limit", "250000")
        )), PolicyFacts.builder().put(FactKey.IS_STUDENT, true).build());

        assertEquals(PolicyResult.SATISFIED, evaluation.result());
    }

    @Test
    void failedOrUnknownIsUnknown() {
        PolicyEvaluation evaluation = evaluate(new AnyOfRule("root", "Any recorded condition", List.of(
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true),
                incomeUnder("income", "Income limit", "250000")
        )), PolicyFacts.builder().put(FactKey.IS_STUDENT, false).build());

        assertEquals(PolicyResult.UNKNOWN, evaluation.result());
    }

    @Test
    void notPreservesUnknownAndInvertsKnownOutcomes() {
        PolicyEvaluation unknown = evaluate(new NotRule("not-student", "Not a student",
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true)), PolicyFacts.builder().build());
        PolicyEvaluation satisfied = evaluate(new NotRule("not-student", "Not a student",
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true)),
                PolicyFacts.builder().put(FactKey.IS_STUDENT, false).build());

        assertEquals(PolicyResult.UNKNOWN, unknown.result());
        assertEquals(PolicyResult.SATISFIED, satisfied.result());
    }

    @Test
    void calculatesAgeUsingTheExplicitEvaluationDate() {
        AgeRangeRule rule = new AgeRangeRule("age", "Age 18 to 20", 18, 20);
        PolicyFacts facts = PolicyFacts.builder().put(FactKey.BIRTH_DATE, LocalDate.of(2006, 10, 12)).build();

        assertEquals(PolicyResult.SATISFIED, evaluate(rule, facts).result());
        PolicyEvaluation later = engine.evaluate(rule, new PolicyEvaluationContext(facts, LocalDate.of(2026, 10, 12)));
        assertEquals(PolicyResult.SATISFIED, later.result());
        PolicyEvaluation boundary = engine.evaluate(new AgeRangeRule("age", "Age 20", 20, 20),
                new PolicyEvaluationContext(facts, LocalDate.of(2026, 10, 11)));
        assertEquals(PolicyResult.FAILED, boundary.result());
    }

    @Test
    void marksConditionalRequirementsNotApplicableWhenTheirConditionFails() {
        ConditionalRule conditional = new ConditionalRule("student-income", "Student income limit",
                new BooleanMatchRule("is-student", "Is a student", FactKey.IS_STUDENT, true),
                incomeUnder("income", "Income limit", "250000"), null);

        PolicyEvaluation evaluation = evaluate(conditional, PolicyFacts.builder()
                .put(FactKey.IS_STUDENT, false)
                .build());

        assertEquals(PolicyResult.NOT_APPLICABLE, evaluation.result());
        assertTrue(evaluation.missingFacts().isEmpty());
    }

    @Test
    void explicitExceptionCanExemptARecordedRequirement() {
        ExceptionRule exceptionRule = new ExceptionRule("income-with-exception", "Income limit except for disability support",
                incomeUnder("income", "Income limit", "250000"),
                new BooleanMatchRule("disability", "Disability exception", FactKey.HAS_DISABILITY, true));

        PolicyEvaluation evaluation = evaluate(exceptionRule, PolicyFacts.builder()
                .put(FactKey.ANNUAL_FAMILY_INCOME, new BigDecimal("500000"))
                .put(FactKey.HAS_DISABILITY, true)
                .build());

        assertEquals(PolicyResult.NOT_APPLICABLE, evaluation.result());
        assertEquals("EXCEPTION_APPLIES", evaluation.trace().reasonCode());
    }

    @Test
    void supportsRequiredAndExcludedCategories() {
        SetMembershipRule included = new SetMembershipRule("included", "Eligible social category", FactKey.CATEGORY,
                MembershipMode.INCLUDED, Set.of("SC", "ST"));
        SetMembershipRule excluded = new SetMembershipRule("excluded", "Excluded category", FactKey.CATEGORY,
                MembershipMode.EXCLUDED, Set.of("GENERAL"));
        PolicyFacts facts = PolicyFacts.builder().put(FactKey.CATEGORY, "SC").build();

        assertEquals(PolicyResult.SATISFIED, evaluate(included, facts).result());
        assertEquals(PolicyResult.SATISFIED, evaluate(excluded, facts).result());
    }

    @Test
    void tracePreservesRuleStructureWithoutReconstructingExplanations() {
        PolicyEvaluation evaluation = evaluate(new AllOfRule("root", "All recorded conditions", List.of(
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true),
                incomeUnder("income", "Income limit", "250000")
        )), PolicyFacts.builder()
                .put(FactKey.IS_STUDENT, true)
                .put(FactKey.ANNUAL_FAMILY_INCOME, new BigDecimal("300000"))
                .build());

        assertEquals(PolicyResult.FAILED, evaluation.result());
        assertEquals("root", evaluation.trace().ruleId());
        assertEquals(2, evaluation.trace().children().size());
        assertEquals("income", evaluation.trace().children().get(1).ruleId());
    }

    private NumericComparisonRule incomeUnder(String id, String label, String amount) {
        return new NumericComparisonRule(id, label, FactKey.ANNUAL_FAMILY_INCOME,
                NumericOperator.LESS_THAN_OR_EQUAL, new BigDecimal(amount));
    }

    private PolicyEvaluation evaluate(com.jansetu.backend.policy.domain.PolicyRule rule, PolicyFacts facts) {
        return engine.evaluate(rule, new PolicyEvaluationContext(facts, evaluationDate));
    }
}
