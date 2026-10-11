package com.jansetu.backend.policy.validation;

/** A publication-blocking structural or type error in a policy tree. */
public record PolicyValidationIssue(String ruleId, String code, String message) { }
