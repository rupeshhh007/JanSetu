package com.jansetu.backend.scheme.service;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import org.springframework.stereotype.Service;

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

    public List<SchemeResponse> getEligibleSchemes(User user) {
        // 1. Fetch user profile entity directly using the User object
        CitizenProfile profile = profileService.getProfileEntity(user);

        // 2. Fetch raw scheme entities
        List<Scheme> allSchemes = schemeService.getAllSchemesEntities();

        // 3. Filter using the evaluator and map to DTOs
        return allSchemes.stream()
                .filter(scheme -> evaluator.isEligible(profile, scheme.getEligibilityCriteria()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SchemeResponse mapToResponse(Scheme scheme) {
        return new SchemeResponse(
                scheme.getId(),
                scheme.getName(),
                scheme.getDescription(),
                scheme.getEligibilityCriteria()
        );
    }
}