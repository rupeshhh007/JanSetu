package com.jansetu.backend.scheme.service;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final CitizenProfileService profileService;
    private final SchemeService schemeService;
    private final EligibilityEvaluator evaluator;

    public MatchingService(CitizenProfileService profileService,
                           SchemeService schemeService,
                           EligibilityEvaluator evaluator) {
        this.profileService = profileService;
        this.schemeService = schemeService;
        this.evaluator = evaluator;
    }

    public List<RecommendationResponse> getRecommendations(User user) {
        // 1. Fetch user profile entity
        CitizenProfile profile = profileService.getProfileEntity(user);

        // 2. Fetch raw scheme entities
        List<Scheme> allSchemes = schemeService.getAllSchemesEntities();

        // 3. Filter using your evaluator, map to DTO, and wrap in RecommendationResponse
        return allSchemes.stream()
                .filter(scheme -> evaluator.isEligible(profile, scheme.getEligibilityCriteria()))
                .map(scheme -> {
                    SchemeResponse schemeDto = mapToResponse(scheme);

                    // ✅ FIX: Actually generate the dynamic reasons!
                    List<String> dynamicReasons = generateMatchReasons(profile, scheme.getEligibilityCriteria());

                    return RecommendationResponse.builder()
                            .scheme(schemeDto)
                            .matchPercentage(100)
                            .matchedCriteria(dynamicReasons) // ✅ FIX: Pass the dynamically generated list here!
                            .build();
                })
                .collect(Collectors.toList());
    }

    // --- NEW HELPER METHOD ---
    private List<String> generateMatchReasons(CitizenProfile profile, EligibilityCriteria criteria) {
        List<String> reasons = new ArrayList<>();

        // 1. Boolean Flags
        if (Boolean.TRUE.equals(criteria.getRequiresFarmer())) {
            reasons.add("You are a registered farmer.");
        }

        if (Boolean.TRUE.equals(criteria.getRequiresStudent())) {
            reasons.add("You are currently a student.");
        }

        if (Boolean.TRUE.equals(criteria.getRequiresDisability())) {
            reasons.add("You meet the specific disability criteria.");
        }

        // 2. Income limits
        if (criteria.getMaxFamilyIncome() != null) {
            reasons.add(String.format("Your annual family income is below ₹%,d.", criteria.getMaxFamilyIncome().longValue()));
        }

        // 3. Lists (Category and Occupation)
        if (criteria.getRequiredCategories() != null && !criteria.getRequiredCategories().isEmpty()) {
            // Since they passed the evaluator check, we know their specific category is in the list
            reasons.add("You belong to the eligible " + profile.getCategory() + " category.");
        }

        if (criteria.getAllowedOccupations() != null && !criteria.getAllowedOccupations().isEmpty()) {
            reasons.add("Your primary occupation (" + profile.getPrimaryOccupation() + ") is eligible.");
        }

        // 4. Age limits
        if (criteria.getMinAge() != null || criteria.getMaxAge() != null) {
            reasons.add("Your age falls within the required bracket for this scheme.");
        }

        // 5. Fallback
        if (reasons.isEmpty()) {
            reasons.add("You meet the baseline eligibility requirements for this scheme.");
        }

        return reasons;
    }

    // Helper method
    private SchemeResponse mapToResponse(Scheme scheme) {
        return new SchemeResponse(
                scheme.getId(),
                scheme.getName(),
                scheme.getDescription(),
                scheme.getEligibilityCriteria()
        );
    }
}