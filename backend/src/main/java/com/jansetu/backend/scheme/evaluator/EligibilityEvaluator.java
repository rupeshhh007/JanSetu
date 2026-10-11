package com.jansetu.backend.scheme.evaluator;

import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Component
public class EligibilityEvaluator {

    /** Shared jurisdiction and publication checks for discovery, chat and submission. */
    public EligibilityEvaluation evaluateScheme(CitizenProfile profile, com.jansetu.backend.scheme.entity.Scheme scheme) {
        if (scheme.getStatus() != com.jansetu.backend.scheme.entity.SchemeStatus.PUBLISHED) {
            return new EligibilityEvaluation(EligibilityOutcome.NOT_ELIGIBLE, List.of(),
                    List.of("This scheme is not currently published."), List.of(), 0);
        }
        if (profile == null) {
            return new EligibilityEvaluation(EligibilityOutcome.NEEDS_INFORMATION, List.of(), List.of(),
                    List.of("Create your citizen profile to check the recorded requirements."), 0);
        }
        String state = scheme.getState();
        if (state != null && !state.isBlank() && !state.equalsIgnoreCase("India") && !state.equalsIgnoreCase("Central")
                && (profile.getState() == null || !state.equalsIgnoreCase(profile.getState().trim()))) {
            return new EligibilityEvaluation(EligibilityOutcome.NOT_ELIGIBLE, List.of(),
                    List.of("This scheme is available to residents of " + state + "."), List.of(), 1);
        }
        return evaluate(profile, scheme.getEligibilityCriteria());
    }

    public boolean isEligible(CitizenProfile profile, EligibilityCriteria criteria) {
        return evaluate(profile, criteria).isEligible();
    }

    /**
     * Evaluates only the rules recorded in JanSetu.  A missing profile value is
     * never silently treated as a match; it becomes a follow-up requirement.
     */
    public EligibilityEvaluation evaluate(CitizenProfile profile, EligibilityCriteria criteria) {

        List<String> matched = new ArrayList<>();
        List<String> unmet = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int evaluatedRules = 0;

        if (criteria == null) {
            return new EligibilityEvaluation(
                    EligibilityOutcome.NEEDS_INFORMATION,
                    matched,
                    unmet,
                    List.of("The scheme's eligibility rules have not been recorded yet."),
                    evaluatedRules
            );
        }

        // 1. Age Check
        if (criteria.getMinAge() != null || criteria.getMaxAge() != null) {
            evaluatedRules++;
            if (profile.getDateOfBirth() == null) {
                missing.add("Date of birth is needed to check the age requirement.");
            } else {
                int age = Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears();
                if (criteria.getMinAge() != null && age < criteria.getMinAge()) {
                    unmet.add("You must be at least " + criteria.getMinAge() + " years old.");
                } else if (criteria.getMaxAge() != null && age > criteria.getMaxAge()) {
                    unmet.add("You must be " + criteria.getMaxAge() + " years old or younger.");
                } else {
                    matched.add("Your age meets the recorded requirement.");
                }
            }
        }

        // 2. Income Check
        if (criteria.getMaxFamilyIncome() != null) {
            evaluatedRules++;
            if (profile.getAnnualFamilyIncome() == null) {
                missing.add("Annual family income is needed to check the income limit.");
            } else if (profile.getAnnualFamilyIncome().compareTo(criteria.getMaxFamilyIncome()) > 0) {
                unmet.add("Annual family income must not exceed ₹"
                        + criteria.getMaxFamilyIncome().toPlainString() + ".");
            } else {
                matched.add("Your recorded family income is within the scheme's limit.");
            }
        }

        // 3. Category Check
        if (criteria.getRequiredCategories() != null && !criteria.getRequiredCategories().isEmpty()) {
            evaluatedRules++;
            if (profile.getCategory() == null) {
                missing.add("Social category is needed to check this scheme requirement.");
            } else if (!criteria.getRequiredCategories().contains(profile.getCategory())) {
                unmet.add("This scheme is recorded for specific social categories.");
            } else {
                matched.add("Your recorded social category is included in the scheme rules.");
            }
        }

        // 4. Occupation Check
        if (criteria.getAllowedOccupations() != null && !criteria.getAllowedOccupations().isEmpty()) {
            evaluatedRules++;
            if (profile.getPrimaryOccupation() == null) {
                missing.add("Primary occupation is needed to check this scheme requirement.");
            } else if (!criteria.getAllowedOccupations().contains(profile.getPrimaryOccupation())) {
                unmet.add("Your recorded occupation is not among this scheme's listed occupations.");
            } else {
                matched.add("Your recorded occupation is included in the scheme rules.");
            }
        }

        // 5. Boolean Flag Checks
        if (Boolean.TRUE.equals(criteria.getRequiresStudent())) {
            evaluatedRules++;
            if (profile.isStudent()) {
                matched.add("You are recorded as a student.");
            } else {
                unmet.add("This scheme requires the applicant to be a student.");
            }
        }

        if (Boolean.TRUE.equals(criteria.getRequiresFarmer())) {
            evaluatedRules++;
            if (profile.isFarmer()) {
                matched.add("You are recorded as a farmer.");
            } else {
                unmet.add("This scheme requires the applicant to be a farmer.");
            }
        }

        if (Boolean.TRUE.equals(criteria.getRequiresDisability())) {
            evaluatedRules++;
            if (profile.isHasDisability()) {
                matched.add("You are recorded as a person with a disability.");
            } else {
                unmet.add("This scheme requires the applicant to have a disability.");
            }
        }

        if (!unmet.isEmpty()) {
            return new EligibilityEvaluation(EligibilityOutcome.NOT_ELIGIBLE, matched, unmet, missing, evaluatedRules);
        }
        if (!missing.isEmpty()) {
            return new EligibilityEvaluation(EligibilityOutcome.NEEDS_INFORMATION, matched, unmet, missing, evaluatedRules);
        }
        if (evaluatedRules == 0) {
            return new EligibilityEvaluation(
                    EligibilityOutcome.NEEDS_INFORMATION,
                    matched,
                    unmet,
                    List.of("The scheme's eligibility rules have not been recorded yet."),
                    evaluatedRules
            );
        }
        return new EligibilityEvaluation(EligibilityOutcome.ELIGIBLE, matched, unmet, missing, evaluatedRules);
    }
}
