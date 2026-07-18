package com.jansetu.backend.scheme.evaluator;

import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class EligibilityEvaluator {

    public boolean isEligible(CitizenProfile profile, EligibilityCriteria criteria) {

        // 1. Age Check
        if (criteria.getMinAge() != null || criteria.getMaxAge() != null) {
            int age = Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears();

            if (criteria.getMinAge() != null && age < criteria.getMinAge()) return false;
            if (criteria.getMaxAge() != null && age > criteria.getMaxAge()) return false;
        }

        // 2. Income Check
        if (criteria.getMaxFamilyIncome() != null) {
            // compareTo returns -1 if less, 0 if equal, 1 if greater
            if (profile.getAnnualFamilyIncome().compareTo(criteria.getMaxFamilyIncome()) > 0) {
                return false;
            }
        }

        // 3. Category Check
        if (criteria.getRequiredCategories() != null && !criteria.getRequiredCategories().isEmpty()) {
            if (!criteria.getRequiredCategories().contains(profile.getCategory())) {
                return false;
            }
        }

        // 4. Occupation Check
        if (criteria.getAllowedOccupations() != null && !criteria.getAllowedOccupations().isEmpty()) {
            if (!criteria.getAllowedOccupations().contains(profile.getPrimaryOccupation())) {
                return false;
            }
        }

        // 5. Boolean Flag Checks
        if (criteria.getRequiresStudent() != null && criteria.getRequiresStudent() && !profile.isStudent()) {
            return false;
        }

        if (criteria.getRequiresFarmer() != null && criteria.getRequiresFarmer() && !profile.isFarmer()) {
            return false;
        }

        if (criteria.getRequiresDisability() != null && criteria.getRequiresDisability() && !profile.isHasDisability()) {
            return false;
        }

        // If we survived all checks, the citizen is eligible
        return true;
    }
}