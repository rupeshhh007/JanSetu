package com.jansetu.backend.scheme.evaluator;

import com.jansetu.backend.profile.entity.Category;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.entity.Occupation;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EligibilityEvaluatorTest {

    private EligibilityEvaluator evaluator;
    private CitizenProfile profile;

    @BeforeEach
    void setUp() {
        evaluator = new EligibilityEvaluator();
        profile = new CitizenProfile();

        // Default valid profile setup
        profile.setDateOfBirth(LocalDate.now().minusYears(20)); // 20 years old
        profile.setAnnualFamilyIncome(new BigDecimal("200000.00"));
        profile.setCategory(Category.SC);
        profile.setPrimaryOccupation(Occupation.STUDENT);
        profile.setStudent(true);
        profile.setFarmer(false);
        profile.setHasDisability(false);
    }

    @Test
    void shouldReturnTrueWhenAllCriteriaMatch() {
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setMinAge(18);
        criteria.setMaxFamilyIncome(new BigDecimal("250000.00"));
        criteria.setRequiredCategories(List.of(Category.SC, Category.ST));
        criteria.setRequiresStudent(true);

        assertTrue(evaluator.isEligible(profile, criteria));
    }

    @Test
    void shouldReturnFalseWhenIncomeIsTooHigh() {
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setMaxFamilyIncome(new BigDecimal("150000.00")); // Criteria is 1.5L, Profile is 2L

        assertFalse(evaluator.isEligible(profile, criteria));
    }

    @Test
    void shouldReturnFalseWhenAgeIsBelowMinimum() {
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setMinAge(25); // Criteria is 25, Profile is 20

        assertFalse(evaluator.isEligible(profile, criteria));
    }

    @Test
    void shouldReturnFalseWhenCategoryDoesNotMatch() {
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setRequiredCategories(List.of(Category.GENERAL)); // Profile is SC

        assertFalse(evaluator.isEligible(profile, criteria));
    }

    @Test
    void shouldReturnTrueWhenCriteriaAreNull() {
        // If criteria fields are null, they should be ignored (the rule is not enforced)
        EligibilityCriteria criteria = new EligibilityCriteria();

        assertTrue(evaluator.isEligible(profile, criteria));
    }
}