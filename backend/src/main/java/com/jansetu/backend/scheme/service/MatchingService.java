package com.jansetu.backend.scheme.service;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;
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

        // Eligibility is deterministic and explainable.  The percentage remains
        // a backwards-compatible display of completed recorded checks, not an
        // AI confidence or a promise of approval.
        return allSchemes.stream()
                .filter(scheme -> scheme.getStatus() == null || scheme.getStatus() == SchemeStatus.PUBLISHED)
                .filter(scheme -> isAvailableInProfileJurisdiction(profile, scheme))
                .map(scheme -> new EvaluatedScheme(
                        scheme,
                        evaluator.evaluateScheme(profile, scheme)
                ))
                .filter(item -> item.evaluation().isEligible())
                .map(scheme -> {
                    SchemeResponse schemeDto = mapToResponse(scheme.scheme());
                    EligibilityEvaluation evaluation = scheme.evaluation();

                    return RecommendationResponse.builder()
                            .scheme(schemeDto)
                            .matchPercentage(compliancePercentage(evaluation))
                            .matchedCriteria(evaluation.matchedCriteria())
                            .eligibilityStatus(evaluation.outcome().name())
                            .missingInformation(evaluation.missingInformation())
                            .build();
                })
                .sorted(Comparator
                        .comparingInt(RecommendationResponse::getMatchPercentage).reversed()
                        .thenComparing(item -> item.getScheme().getName()))
                .collect(Collectors.toList());
    }

    private boolean isAvailableInProfileJurisdiction(CitizenProfile profile, Scheme scheme) {
        String jurisdiction = scheme.getState();
        if (jurisdiction == null || jurisdiction.isBlank()
                || jurisdiction.equalsIgnoreCase("India")
                || jurisdiction.equalsIgnoreCase("Central")) {
            return true;
        }
        return profile.getState() != null && jurisdiction.equalsIgnoreCase(profile.getState().trim());
    }

    private int compliancePercentage(EligibilityEvaluation evaluation) {
        // Every recorded applicable rule has been satisfied; this field is kept
        // only for the current UI contract.  Consumers should use
        // eligibilityStatus for the decision itself.
        return evaluation.isEligible() ? 100 : 0;
    }

    private record EvaluatedScheme(Scheme scheme, EligibilityEvaluation evaluation) { }

    // Helper method
    private SchemeResponse mapToResponse(Scheme scheme) {
        return SchemeResponse.builder()
                .id(scheme.getId())
                .name(scheme.getName())
                .description(scheme.getDescription())
                .state(scheme.getState())
                .department(scheme.getDepartment())
                .officialUrl(scheme.getOfficialUrl())
                .sourceName(scheme.getSourceName())
                .lastVerifiedAt(scheme.getLastVerifiedAt())
                .benefits(scheme.getBenefits())
                .requiredDocuments(scheme.getRequiredDocuments())
                .status(scheme.getStatus())
                .publishedAt(scheme.getPublishedAt())
                .archivedAt(scheme.getArchivedAt())
                .updatedAt(scheme.getUpdatedAt())
                .eligibilityCriteria(scheme.getEligibilityCriteria())
                .build();
    }
}
