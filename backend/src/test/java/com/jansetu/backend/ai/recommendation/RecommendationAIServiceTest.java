package com.jansetu.backend.ai.recommendation;

import com.jansetu.backend.ai.dto.ChatResult;
import com.jansetu.backend.application.repository.ApplicationRepository;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.dto.RecommendationState;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.service.MatchingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationAIServiceTest {

    @Test
    void onlyOffersReadySchemesAsApplicationCardsAndExplainsOtherStates() {
        TestFixture fixture = fixture();
        RecommendationResponse ready = recommendation("Education support", RecommendationState.READY_TO_REVIEW,
                List.of("Age recorded"));
        RecommendationResponse needsProfile = recommendation("Income support", RecommendationState.PROFILE_INFORMATION_NEEDED,
                List.of("Annual income is needed"));
        RecommendationResponse needsPolicy = recommendation("Housing support", RecommendationState.POLICY_INFORMATION_UNAVAILABLE,
                List.of("Policy details are not recorded"));
        when(fixture.matching.getRecommendations(fixture.user)).thenReturn(List.of(ready, needsProfile, needsPolicy));

        ChatResult result = fixture.service.generateRecommendation();

        assertTrue(result.response().contains("Schemes ready to review"));
        assertTrue(result.response().contains("Complete your profile"));
        assertTrue(result.response().contains("Official policy review needed"));
        assertFalse(result.response().contains("eligible schemes"));
        assertEquals(1, result.cards().size());
        assertEquals("Education support", result.cards().getFirst().title());
    }

    @Test
    void doesNotPresentIncompleteOrUnrecordedPoliciesAsEligible() {
        TestFixture fixture = fixture();
        when(fixture.matching.getRecommendations(fixture.user)).thenReturn(List.of(
                recommendation("Income support", RecommendationState.PROFILE_INFORMATION_NEEDED,
                        List.of("Annual income is needed")),
                recommendation("Housing support", RecommendationState.POLICY_INFORMATION_UNAVAILABLE,
                        List.of("Policy details are not recorded"))
        ));

        ChatResult result = fixture.service.generateRecommendation();

        assertTrue(result.response().contains("What JanSetu can confirm"));
        assertTrue(result.response().contains("No scheme is ready to review"));
        assertFalse(result.response().contains("eligible schemes"));
        assertTrue(result.cards().isEmpty());
    }

    private TestFixture fixture() {
        MatchingService matching = mock(MatchingService.class);
        CitizenProfileService profiles = mock(CitizenProfileService.class);
        ApplicationRepository applications = mock(ApplicationRepository.class);
        CurrentUserService users = mock(CurrentUserService.class);
        User user = new User();
        CitizenProfile profile = new CitizenProfile();
        when(users.getCurrentUser()).thenReturn(user);
        when(profiles.getProfileEntity(user)).thenReturn(profile);
        when(applications.findByCitizenProfile(profile)).thenReturn(List.of());
        return new TestFixture(
                user,
                matching,
                new RecommendationAIService(matching, profiles, applications, users)
        );
    }

    private RecommendationResponse recommendation(
            String schemeName,
            RecommendationState state,
            List<String> missingInformation
    ) {
        return RecommendationResponse.builder()
                .scheme(SchemeResponse.builder()
                        .id(UUID.randomUUID())
                        .name(schemeName)
                        .description("Recorded scheme description")
                        .officialUrl("https://example.gov.in/scheme")
                        .build())
                .recommendationState(state)
                .missingInformation(missingInformation)
                .matchedCriteria(state == RecommendationState.READY_TO_REVIEW ? List.of("Age recorded") : List.of())
                .unmetCriteria(List.of())
                .evidenceScore(state == RecommendationState.READY_TO_REVIEW ? 100 : 0)
                .recordedRuleCount(state == RecommendationState.POLICY_INFORMATION_UNAVAILABLE ? 0 : 1)
                .build();
    }

    private record TestFixture(
            User user,
            MatchingService matching,
            RecommendationAIService service
    ) { }
}
