package com.jansetu.backend.scheme.policy;

import com.jansetu.backend.policy.domain.AgeRangeRule;
import com.jansetu.backend.policy.domain.AllOfRule;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.MembershipMode;
import com.jansetu.backend.policy.domain.NumericComparisonRule;
import com.jansetu.backend.policy.domain.NumericOperator;
import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.domain.SetMembershipRule;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transitional adapter for the original catalogue JSON. It keeps existing
 * records working while using the typed evaluator rather than a parallel
 * ad-hoc algorithm.
 */
@Component
public class LegacyEligibilityPolicyAdapter {
    public PolicyRule toPolicy(EligibilityCriteria criteria) {
        if (criteria == null) {
            return null;
        }
        List<PolicyRule> rules = new ArrayList<>();
        if (criteria.getMinAge() != null || criteria.getMaxAge() != null) {
            rules.add(new AgeRangeRule("legacy-age", "Recorded age requirement",
                    criteria.getMinAge(), criteria.getMaxAge()));
        }
        if (criteria.getMaxFamilyIncome() != null) {
            rules.add(new NumericComparisonRule("legacy-income", "Recorded family-income limit",
                    FactKey.ANNUAL_FAMILY_INCOME, NumericOperator.LESS_THAN_OR_EQUAL,
                    criteria.getMaxFamilyIncome()));
        }
        if (criteria.getRequiredCategories() != null && !criteria.getRequiredCategories().isEmpty()) {
            rules.add(new SetMembershipRule("legacy-category", "Recorded social-category requirement",
                    FactKey.CATEGORY, MembershipMode.INCLUDED, criteria.getRequiredCategories().stream()
                    .map(Enum::name).collect(Collectors.toUnmodifiableSet())));
        }
        if (criteria.getAllowedOccupations() != null && !criteria.getAllowedOccupations().isEmpty()) {
            rules.add(new SetMembershipRule("legacy-occupation", "Recorded occupation requirement",
                    FactKey.PRIMARY_OCCUPATION, MembershipMode.INCLUDED, criteria.getAllowedOccupations().stream()
                    .map(Enum::name).collect(Collectors.toUnmodifiableSet())));
        }
        if (Boolean.TRUE.equals(criteria.getRequiresStudent())) {
            rules.add(new BooleanMatchRule("legacy-student", "Recorded student requirement", FactKey.IS_STUDENT, true));
        }
        if (Boolean.TRUE.equals(criteria.getRequiresFarmer())) {
            rules.add(new BooleanMatchRule("legacy-farmer", "Recorded farmer requirement", FactKey.IS_FARMER, true));
        }
        if (Boolean.TRUE.equals(criteria.getRequiresDisability())) {
            rules.add(new BooleanMatchRule("legacy-disability", "Recorded disability requirement", FactKey.HAS_DISABILITY, true));
        }
        return rules.isEmpty() ? null : new AllOfRule("legacy-recorded-criteria", "Recorded scheme conditions", rules);
    }
}
