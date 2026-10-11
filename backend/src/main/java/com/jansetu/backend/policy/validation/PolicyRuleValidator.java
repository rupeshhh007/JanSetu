package com.jansetu.backend.policy.validation;

import com.jansetu.backend.policy.domain.AgeRangeRule;
import com.jansetu.backend.policy.domain.AllOfRule;
import com.jansetu.backend.policy.domain.AnyOfRule;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.ConditionalRule;
import com.jansetu.backend.policy.domain.DateRangeRule;
import com.jansetu.backend.policy.domain.ExceptionRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.FactValueType;
import com.jansetu.backend.policy.domain.NotRule;
import com.jansetu.backend.policy.domain.NumericComparisonRule;
import com.jansetu.backend.policy.domain.NumericRangeRule;
import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.domain.RequiredFactRule;
import com.jansetu.backend.policy.domain.SetMembershipRule;
import com.jansetu.backend.policy.domain.TextComparisonRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates the closed policy AST before review approval or publication. */
@Component
public class PolicyRuleValidator {

    public List<PolicyValidationIssue> validate(PolicyRule root) {
        List<PolicyValidationIssue> issues = new ArrayList<>();
        if (root == null) {
            issues.add(new PolicyValidationIssue(null, "POLICY_REQUIRED", "A policy root rule is required."));
            return List.copyOf(issues);
        }
        validateNode(root, new HashSet<>(), issues);
        return List.copyOf(issues);
    }

    public void validateOrThrow(PolicyRule root) {
        List<PolicyValidationIssue> issues = validate(root);
        if (!issues.isEmpty()) {
            throw new InvalidPolicyException(issues);
        }
    }

    private void validateNode(PolicyRule rule, Set<String> seenIds, List<PolicyValidationIssue> issues) {
        if (rule == null) {
            issues.add(new PolicyValidationIssue(null, "RULE_REQUIRED", "Policy rule children cannot be null."));
            return;
        }
        String id = rule.id();
        if (id == null || id.isBlank()) {
            issue(issues, id, "RULE_ID_REQUIRED", "Every policy rule needs a stable identifier.");
        } else if (!seenIds.add(id)) {
            issue(issues, id, "DUPLICATE_RULE_ID", "Rule identifiers must be unique within a policy version.");
        }
        if (rule.label() == null || rule.label().isBlank()) {
            issue(issues, id, "RULE_LABEL_REQUIRED", "Every policy rule needs a citizen-readable label.");
        }

        if (rule instanceof AllOfRule allOf) {
            validateLogicalChildren(id, "AND", allOf.children(), seenIds, issues);
        } else if (rule instanceof AnyOfRule anyOf) {
            validateLogicalChildren(id, "OR", anyOf.children(), seenIds, issues);
        } else if (rule instanceof NotRule not) {
            validateNode(not.child(), seenIds, issues);
        } else if (rule instanceof RequiredFactRule required) {
            requireFactType(required.fact(), null, id, issues);
        } else if (rule instanceof NumericComparisonRule numeric) {
            requireFactType(numeric.fact(), FactValueType.DECIMAL, id, issues);
            if (numeric.expectedValue() == null) {
                issue(issues, id, "NUMERIC_VALUE_REQUIRED", "A numeric comparison needs an expected value.");
            }
        } else if (rule instanceof NumericRangeRule range) {
            requireFactType(range.fact(), FactValueType.DECIMAL, id, issues);
            if (range.minimum() == null && range.maximum() == null) {
                issue(issues, id, "RANGE_BOUND_REQUIRED", "A numeric range needs at least one bound.");
            } else if (range.minimum() != null && range.maximum() != null
                    && range.minimum().compareTo(range.maximum()) > 0) {
                issue(issues, id, "INVALID_NUMERIC_RANGE", "The lower numeric bound must not exceed the upper bound.");
            }
        } else if (rule instanceof DateRangeRule range) {
            requireFactType(range.fact(), FactValueType.DATE, id, issues);
            if (range.earliest() == null && range.latest() == null) {
                issue(issues, id, "RANGE_BOUND_REQUIRED", "A date range needs at least one bound.");
            } else if (range.earliest() != null && range.latest() != null && range.earliest().isAfter(range.latest())) {
                issue(issues, id, "INVALID_DATE_RANGE", "The earliest date must not be after the latest date.");
            }
        } else if (rule instanceof AgeRangeRule age) {
            validateAgeRange(age, issues);
        } else if (rule instanceof TextComparisonRule text) {
            requireFactType(text.fact(), FactValueType.TEXT, id, issues);
            if (text.expectedValue() == null || text.expectedValue().isBlank()) {
                issue(issues, id, "TEXT_VALUE_REQUIRED", "A text comparison needs an expected value.");
            }
        } else if (rule instanceof SetMembershipRule membership) {
            requireFactType(membership.fact(), FactValueType.TEXT, id, issues);
            if (membership.values().isEmpty() || membership.values().stream().anyMatch(value -> value == null || value.isBlank())) {
                issue(issues, id, "MEMBERSHIP_VALUES_REQUIRED", "A membership rule needs one or more nonblank values.");
            }
            if (membership.mode() == null) {
                issue(issues, id, "MEMBERSHIP_MODE_REQUIRED", "A membership rule needs an inclusion or exclusion mode.");
            }
        } else if (rule instanceof BooleanMatchRule booleanRule) {
            requireFactType(booleanRule.fact(), FactValueType.BOOLEAN, id, issues);
        } else if (rule instanceof ConditionalRule conditional) {
            validateNode(conditional.condition(), seenIds, issues);
            validateNode(conditional.whenTrue(), seenIds, issues);
            if (conditional.whenFalse() != null) {
                validateNode(conditional.whenFalse(), seenIds, issues);
            }
        } else if (rule instanceof ExceptionRule exceptionRule) {
            validateNode(exceptionRule.baseRequirement(), seenIds, issues);
            validateNode(exceptionRule.exemptionCondition(), seenIds, issues);
        }
    }

    private void validateLogicalChildren(String id, String operator, List<PolicyRule> children,
                                         Set<String> seenIds, List<PolicyValidationIssue> issues) {
        if (children.isEmpty()) {
            issue(issues, id, "EMPTY_" + operator, operator + " requires at least one child rule.");
            return;
        }
        children.forEach(child -> validateNode(child, seenIds, issues));
    }

    private void validateAgeRange(AgeRangeRule age, List<PolicyValidationIssue> issues) {
        if (age.minimumAge() == null && age.maximumAge() == null) {
            issue(issues, age.id(), "AGE_BOUND_REQUIRED", "An age range needs at least one bound.");
        }
        if ((age.minimumAge() != null && (age.minimumAge() < 0 || age.minimumAge() > 150))
                || (age.maximumAge() != null && (age.maximumAge() < 0 || age.maximumAge() > 150))) {
            issue(issues, age.id(), "INVALID_AGE_BOUND", "Age bounds must be between 0 and 150.");
        }
        if (age.minimumAge() != null && age.maximumAge() != null && age.minimumAge() > age.maximumAge()) {
            issue(issues, age.id(), "INVALID_AGE_RANGE", "The minimum age must not exceed the maximum age.");
        }
    }

    private void requireFactType(FactKey fact, FactValueType expectedType, String id,
                                 List<PolicyValidationIssue> issues) {
        if (fact == null) {
            issue(issues, id, "FACT_REQUIRED", "A policy fact is required.");
        } else if (expectedType != null && fact.valueType() != expectedType) {
            issue(issues, id, "FACT_TYPE_MISMATCH", "This rule requires a " + expectedType + " fact.");
        }
    }

    private void issue(List<PolicyValidationIssue> issues, String ruleId, String code, String message) {
        issues.add(new PolicyValidationIssue(ruleId, code, message));
    }
}
