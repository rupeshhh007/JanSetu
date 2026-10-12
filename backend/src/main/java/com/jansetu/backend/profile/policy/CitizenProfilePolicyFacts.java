package com.jansetu.backend.profile.policy;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyFactSnapshot;
import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.fact.CitizenProfileFact;

import java.util.Collection;
import java.util.List;

/** Maps the profile aggregate into the typed input boundary of the policy module. */
public final class CitizenProfilePolicyFacts {
    private CitizenProfilePolicyFacts() { }

    public static PolicyFacts from(CitizenProfile profile) {
        return baseFacts(profile).build();
    }

    /** Active, typed facts override legacy profile fields without mutating the profile aggregate. */
    public static PolicyFacts from(CitizenProfile profile, Collection<CitizenProfileFact> profileFacts) {
        PolicyFacts.Builder facts = baseFacts(profile);
        if (profileFacts != null) {
            for (CitizenProfileFact fact : profileFacts) {
                if (fact == null || fact.getFactKey() == null || fact.getValueText() == null) {
                    continue;
                }
                Object value = PolicyFacts.fromSnapshot(List.of(
                        new PolicyFactSnapshot(fact.getFactKey(), fact.getValueText())
                )).value(fact.getFactKey()).orElseThrow();
                facts.put(fact.getFactKey(), value);
            }
        }
        return facts.build();
    }

    private static PolicyFacts.Builder baseFacts(CitizenProfile profile) {
        return PolicyFacts.builder()
                .put(FactKey.BIRTH_DATE, profile.getDateOfBirth())
                .put(FactKey.ANNUAL_FAMILY_INCOME, profile.getAnnualFamilyIncome())
                .put(FactKey.CATEGORY, profile.getCategory())
                .put(FactKey.PRIMARY_OCCUPATION, profile.getPrimaryOccupation())
                .put(FactKey.STATE, profile.getState())
                .put(FactKey.IS_STUDENT, profile.isStudent())
                .put(FactKey.IS_FARMER, profile.isFarmer())
                .put(FactKey.HAS_DISABILITY, profile.isHasDisability());
    }
}
