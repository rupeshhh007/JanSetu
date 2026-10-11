package com.jansetu.backend.policy.validation;

import com.jansetu.backend.policy.domain.AllOfRule;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.NumericRangeRule;
import com.jansetu.backend.policy.domain.PolicyRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyRuleValidatorTest {
    private final PolicyRuleValidator validator = new PolicyRuleValidator();

    @Test
    void acceptsAWellTypedTree() {
        PolicyRule valid = new AllOfRule("root", "Recorded conditions", List.of(
                new NumericRangeRule("income", "Income range", FactKey.ANNUAL_FAMILY_INCOME,
                        null, new BigDecimal("250000")),
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true)
        ));

        assertTrue(validator.validate(valid).isEmpty());
    }

    @Test
    void rejectsEmptyLogicalRulesAndDuplicateIds() {
        PolicyRule invalid = new AllOfRule("root", "Recorded conditions", List.of(
                new AllOfRule("empty", "Empty", List.of()),
                new BooleanMatchRule("dup", "First", FactKey.IS_STUDENT, true),
                new BooleanMatchRule("dup", "Second", FactKey.IS_FARMER, true)
        ));

        List<PolicyValidationIssue> issues = validator.validate(invalid);

        assertEquals(2, issues.size());
        assertTrue(issues.stream().anyMatch(issue -> issue.code().equals("EMPTY_AND")));
        assertTrue(issues.stream().anyMatch(issue -> issue.code().equals("DUPLICATE_RULE_ID")));
    }

    @Test
    void rejectsRulesBoundToTheWrongFactType() {
        PolicyRule invalid = new NumericRangeRule("income", "Income range", FactKey.CATEGORY,
                new BigDecimal("1"), new BigDecimal("2"));

        InvalidPolicyException exception = assertThrows(InvalidPolicyException.class,
                () -> validator.validateOrThrow(invalid));

        assertEquals("FACT_TYPE_MISMATCH", exception.issues().getFirst().code());
    }

    @Test
    void rejectsInvertedRanges() {
        PolicyRule invalid = new NumericRangeRule("income", "Income range", FactKey.ANNUAL_FAMILY_INCOME,
                new BigDecimal("250000"), new BigDecimal("100000"));

        assertTrue(validator.validate(invalid).stream()
                .anyMatch(issue -> issue.code().equals("INVALID_NUMERIC_RANGE")));
    }
}
