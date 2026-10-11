package com.jansetu.backend.profile.policy;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.profile.entity.CitizenProfile;

/** Maps the profile aggregate into the typed input boundary of the policy module. */
public final class CitizenProfilePolicyFacts {
    private CitizenProfilePolicyFacts() { }

    public static PolicyFacts from(CitizenProfile profile) {
        return PolicyFacts.builder()
                .put(FactKey.BIRTH_DATE, profile.getDateOfBirth())
                .put(FactKey.ANNUAL_FAMILY_INCOME, profile.getAnnualFamilyIncome())
                .put(FactKey.CATEGORY, profile.getCategory())
                .put(FactKey.PRIMARY_OCCUPATION, profile.getPrimaryOccupation())
                .put(FactKey.STATE, profile.getState())
                .put(FactKey.IS_STUDENT, profile.isStudent())
                .put(FactKey.IS_FARMER, profile.isFarmer())
                .put(FactKey.HAS_DISABILITY, profile.isHasDisability())
                .build();
    }
}
