package com.jansetu.backend.policy.validation;

import java.util.List;

public class InvalidPolicyException extends IllegalArgumentException {
    private final List<PolicyValidationIssue> issues;

    public InvalidPolicyException(List<PolicyValidationIssue> issues) {
        super("Policy validation failed: " + issues.stream()
                .map(PolicyValidationIssue::code)
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("unknown error"));
        this.issues = List.copyOf(issues);
    }

    public List<PolicyValidationIssue> issues() {
        return issues;
    }
}
