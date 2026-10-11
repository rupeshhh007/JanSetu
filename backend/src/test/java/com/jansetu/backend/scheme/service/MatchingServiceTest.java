package com.jansetu.backend.scheme.service;

import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationState;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluation;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.evaluator.EligibilityOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchingServiceTest {
    @Test
    void returnsEligibleAndIncompleteSchemesWithEvidenceBasedStates() {
        CitizenProfileService profiles = mock(CitizenProfileService.class);
        SchemeService schemes = mock(SchemeService.class);
        EligibilityEvaluator evaluator = mock(EligibilityEvaluator.class);
        MatchingService matching = new MatchingService(profiles, schemes, evaluator);
        CitizenProfile profile = new CitizenProfile();
        profile.setState("Maharashtra");
        Scheme ready = scheme("Ready scheme");
        Scheme needsInformation = scheme("Incomplete scheme");
        Scheme unavailable = scheme("Unavailable scheme");

        when(profiles.getProfileEntity(null)).thenReturn(profile);
        when(schemes.getAllSchemesEntities()).thenReturn(List.of(needsInformation, unavailable, ready));
        when(evaluator.evaluateScheme(profile, ready)).thenReturn(new EligibilityEvaluation(EligibilityOutcome.ELIGIBLE,
                List.of("Age recorded", "Income recorded"), List.of(), List.of(), 2, null));
        when(evaluator.evaluateScheme(profile, needsInformation)).thenReturn(new EligibilityEvaluation(EligibilityOutcome.NEEDS_INFORMATION,
                List.of("Age recorded"), List.of(), List.of("Income is needed"), 2, null));
        when(evaluator.evaluateScheme(profile, unavailable)).thenReturn(new EligibilityEvaluation(EligibilityOutcome.NOT_ELIGIBLE,
                List.of(), List.of("Not available"), List.of(), 1, null));

        var recommendations = matching.getRecommendations(null);

        assertEquals(2, recommendations.size());
        assertEquals("Ready scheme", recommendations.getFirst().getScheme().getName());
        assertEquals(RecommendationState.READY_TO_REVIEW, recommendations.getFirst().getRecommendationState());
        assertEquals(100, recommendations.getFirst().getEvidenceScore());
        assertEquals("Incomplete scheme", recommendations.get(1).getScheme().getName());
        assertEquals(RecommendationState.PROFILE_INFORMATION_NEEDED, recommendations.get(1).getRecommendationState());
        assertEquals(50, recommendations.get(1).getEvidenceScore());
        assertTrue(recommendations.get(1).isRequiresProfileAction());
    }

    @Test
    void identifiesAnUnrecordedPolicyInsteadOfSendingTheCitizenToProfileEditing() {
        CitizenProfileService profiles = mock(CitizenProfileService.class);
        SchemeService schemes = mock(SchemeService.class);
        EligibilityEvaluator evaluator = mock(EligibilityEvaluator.class);
        MatchingService matching = new MatchingService(profiles, schemes, evaluator);
        CitizenProfile profile = new CitizenProfile();
        profile.setState("Maharashtra");
        Scheme unrecorded = scheme("Unrecorded scheme");

        when(profiles.getProfileEntity(null)).thenReturn(profile);
        when(schemes.getAllSchemesEntities()).thenReturn(List.of(unrecorded));
        when(evaluator.evaluateScheme(profile, unrecorded)).thenReturn(new EligibilityEvaluation(
                EligibilityOutcome.NEEDS_INFORMATION, List.of(), List.of(),
                List.of("The scheme's eligibility rules have not been recorded yet."), 0, null));

        var recommendation = matching.getRecommendations(null).getFirst();

        assertEquals(RecommendationState.POLICY_INFORMATION_UNAVAILABLE, recommendation.getRecommendationState());
        assertEquals(0, recommendation.getEvidenceScore());
        assertTrue(!recommendation.isRequiresProfileAction());
        assertTrue(recommendation.getNextStep().contains("recorded policy review"));
    }

    private Scheme scheme(String name) {
        Scheme scheme = new Scheme();
        scheme.setName(name);
        scheme.setDescription("Test description");
        scheme.setState("Maharashtra");
        scheme.setDepartment("Test department");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setEligibilityCriteria(new EligibilityCriteria());
        scheme.setBenefits(List.of());
        scheme.setRequiredDocuments(List.of());
        return scheme;
    }
}
