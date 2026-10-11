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
import com.jansetu.backend.scheme.evaluator.EligibilityOutcome;
import com.jansetu.backend.scheme.dto.RecommendationState;
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

        // Preserve incomplete-profile outcomes as an action path instead of
        // silently removing them. A recommendation is never a promise of a
        // government approval.
        return allSchemes.stream()
                .filter(scheme -> scheme.getStatus() == null || scheme.getStatus() == SchemeStatus.PUBLISHED)
                .filter(scheme -> isAvailableInProfileJurisdiction(profile, scheme))
                .map(scheme -> new EvaluatedScheme(
                        scheme,
                        evaluator.evaluateScheme(profile, scheme)
                ))
                .filter(item -> item.evaluation().outcome() != EligibilityOutcome.NOT_ELIGIBLE)
                .map(scheme -> {
                    SchemeResponse schemeDto = mapToResponse(scheme.scheme());
                    EligibilityEvaluation evaluation = scheme.evaluation();
                    int evidenceScore = evidenceScore(evaluation);
                    RecommendationState recommendationState = recommendationState(evaluation);

                    return RecommendationResponse.builder()
                            .scheme(schemeDto)
                            .matchPercentage(evidenceScore)
                            .evidenceScore(evidenceScore)
                            .recordedRuleCount(evaluation.evaluatedRuleCount())
                            .recommendationState(recommendationState)
                            .requiresProfileAction(recommendationState == RecommendationState.PROFILE_INFORMATION_NEEDED)
                            .nextStep(nextStepFor(evaluation))
                            .matchedCriteria(evaluation.matchedCriteria())
                            .unmetCriteria(evaluation.unmetCriteria())
                            .eligibilityStatus(evaluation.outcome().name())
                            .missingInformation(evaluation.missingInformation())
                            .build();
                })
                .sorted(Comparator
                        .comparing(RecommendationResponse::getRecommendationState)
                        .thenComparing(Comparator.comparingInt(RecommendationResponse::getEvidenceScore).reversed())
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

    private int evidenceScore(EligibilityEvaluation evaluation) {
        int total = evaluation.matchedCriteria().size()
                + evaluation.unmetCriteria().size()
                + evaluation.missingInformation().size();
        if (total == 0) {
            return 0;
        }
        return Math.round((evaluation.matchedCriteria().size() * 100f) / total);
    }

    private RecommendationState recommendationState(EligibilityEvaluation evaluation) {
        if (evaluation.outcome() == EligibilityOutcome.ELIGIBLE) {
            return RecommendationState.READY_TO_REVIEW;
        }
        return evaluation.evaluatedRuleCount() == 0
                ? RecommendationState.POLICY_INFORMATION_UNAVAILABLE
                : RecommendationState.PROFILE_INFORMATION_NEEDED;
    }

    private String nextStepFor(EligibilityEvaluation evaluation) {
        if (evaluation.outcome() == EligibilityOutcome.ELIGIBLE) {
            return "Review the recorded requirements and official scheme details before you apply.";
        }
        return evaluation.evaluatedRuleCount() == 0
                ? "This scheme needs a recorded policy review before JanSetu can assess it. Check its official source."
                : "Add the requested profile information to complete this recorded policy check.";
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
