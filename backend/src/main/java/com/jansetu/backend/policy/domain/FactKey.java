package com.jansetu.backend.policy.domain;

/**
 * Citizen facts which may be used by a published policy.  New facts are added
 * deliberately here instead of allowing an unbounded string key space.
 */
public enum FactKey {
    BIRTH_DATE(FactValueType.DATE, "date of birth"),
    ANNUAL_FAMILY_INCOME(FactValueType.DECIMAL, "annual family income"),
    CATEGORY(FactValueType.TEXT, "social category"),
    PRIMARY_OCCUPATION(FactValueType.TEXT, "primary occupation"),
    STATE(FactValueType.TEXT, "state of residence"),
    IS_STUDENT(FactValueType.BOOLEAN, "student status"),
    IS_FARMER(FactValueType.BOOLEAN, "farmer status"),
    HAS_DISABILITY(FactValueType.BOOLEAN, "disability status");

    private final FactValueType valueType;
    private final String displayName;

    FactKey(FactValueType valueType, String displayName) {
        this.valueType = valueType;
        this.displayName = displayName;
    }

    public FactValueType valueType() {
        return valueType;
    }

    public String displayName() {
        return displayName;
    }
}
