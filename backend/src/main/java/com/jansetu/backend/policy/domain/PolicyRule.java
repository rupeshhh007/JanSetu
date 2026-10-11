package com.jansetu.backend.policy.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A constrained policy AST.  Its variants are data-only and deliberately do
 * not permit expressions, reflection, scripting, or dynamically loaded code.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AllOfRule.class, name = "allOf"),
        @JsonSubTypes.Type(value = AnyOfRule.class, name = "anyOf"),
        @JsonSubTypes.Type(value = NotRule.class, name = "not"),
        @JsonSubTypes.Type(value = RequiredFactRule.class, name = "requiredFact"),
        @JsonSubTypes.Type(value = NumericComparisonRule.class, name = "numericComparison"),
        @JsonSubTypes.Type(value = NumericRangeRule.class, name = "numericRange"),
        @JsonSubTypes.Type(value = DateRangeRule.class, name = "dateRange"),
        @JsonSubTypes.Type(value = AgeRangeRule.class, name = "ageRange"),
        @JsonSubTypes.Type(value = TextComparisonRule.class, name = "textComparison"),
        @JsonSubTypes.Type(value = SetMembershipRule.class, name = "setMembership"),
        @JsonSubTypes.Type(value = BooleanMatchRule.class, name = "booleanMatch"),
        @JsonSubTypes.Type(value = ConditionalRule.class, name = "conditional"),
        @JsonSubTypes.Type(value = ExceptionRule.class, name = "exception")
})
public sealed interface PolicyRule permits AllOfRule, AnyOfRule, NotRule,
        RequiredFactRule, NumericComparisonRule, NumericRangeRule, DateRangeRule,
        AgeRangeRule, TextComparisonRule, SetMembershipRule, BooleanMatchRule,
        ConditionalRule, ExceptionRule {
    String id();

    String label();
}
