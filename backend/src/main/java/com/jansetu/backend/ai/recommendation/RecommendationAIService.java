package com.jansetu.backend.ai.recommendation;

import com.jansetu.backend.ai.dto.ChatCard;
import com.jansetu.backend.ai.dto.ChatResult;
import com.jansetu.backend.ai.dto.Citation;
import com.jansetu.backend.application.repository.ApplicationRepository;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.dto.RecommendationState;
import com.jansetu.backend.scheme.dto.SchemeResponse;
import com.jansetu.backend.scheme.service.MatchingService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Recommendation text is intentionally deterministic. A language model may
 * later rephrase it, but it never decides eligibility or invents a scheme.
 */
@Service
public class RecommendationAIService {

    private final MatchingService matchingService;
    private final CitizenProfileService profileService;
    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;

    public RecommendationAIService(
            MatchingService matchingService,
            CitizenProfileService profileService,
            ApplicationRepository applicationRepository,
            CurrentUserService currentUserService
    ) {
        this.matchingService = matchingService;
        this.profileService = profileService;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
    }

    public ChatResult generateRecommendation() {
        User user = currentUserService.getCurrentUser();
        CitizenProfile profile = profileService.getProfileEntity(user);
        List<RecommendationResponse> recommendations = matchingService.getRecommendations(user);
        Set<UUID> appliedSchemeIds = applicationRepository.findByCitizenProfile(profile).stream()
                .filter(application -> application.getStatus() != com.jansetu.backend.application.enums.ApplicationStatus.REJECTED
                        && application.getStatus() != com.jansetu.backend.application.enums.ApplicationStatus.WITHDRAWN)
                .map(application -> application.getScheme().getId())
                .collect(Collectors.toSet());

        List<RecommendationResponse> readyToReview = recommendations.stream()
                .filter(recommendation -> recommendation.getRecommendationState() == RecommendationState.READY_TO_REVIEW)
                .filter(recommendation -> !appliedSchemeIds.contains(recommendation.getScheme().getId()))
                .sorted(Comparator.comparing(item -> item.getScheme().getName()))
                .toList();
        List<RecommendationResponse> profileInformationNeeded = recommendations.stream()
                .filter(recommendation -> recommendation.getRecommendationState()
                        == RecommendationState.PROFILE_INFORMATION_NEEDED)
                .toList();
        List<RecommendationResponse> policyInformationUnavailable = recommendations.stream()
                .filter(recommendation -> recommendation.getRecommendationState()
                        == RecommendationState.POLICY_INFORMATION_UNAVAILABLE)
                .toList();

        if (readyToReview.isEmpty()) {
            return noReadySchemeResult(profileInformationNeeded, policyInformationUnavailable);
        }

        StringBuilder text = new StringBuilder("## Schemes ready to review\n\n")
                .append("These are supported by the policy requirements currently recorded in JanSetu. "
                        + "They are not official approval decisions; confirm the official details before applying.\n\n");
        List<Citation> citations = new ArrayList<>();
        List<ChatCard> cards = new ArrayList<>();

        for (int index = 0; index < readyToReview.size(); index++) {
            RecommendationResponse recommendation = readyToReview.get(index);
            SchemeResponse scheme = recommendation.getScheme();
            text.append(index + 1).append(". **").append(scheme.getName()).append("** — ")
                    .append(scheme.getDescription()).append("\n")
                    .append("   - Why it appears: ")
                    .append(String.join("; ", recommendation.getMatchedCriteria())).append("\n");
            if (scheme.getBenefits() != null && !scheme.getBenefits().isEmpty()) {
                text.append("   - Recorded benefit: ").append(String.join("; ", scheme.getBenefits())).append("\n");
            }
            citations.add(citationFor(scheme));
            cards.add(new ChatCard(
                    "scheme", scheme.getName(), scheme.getDescription(), scheme.getId().toString(),
                    recommendation.getMatchedCriteria(), "View official source", scheme.getOfficialUrl()
            ));
        }

        appendNextSteps(text, profileInformationNeeded, policyInformationUnavailable);

        return new ChatResult(
                text.toString(), List.copyOf(citations), List.copyOf(cards),
                List.of("What documents are recorded for these schemes?", "Compare these schemes"), true
        );
    }

    private ChatResult noReadySchemeResult(
            List<RecommendationResponse> profileInformationNeeded,
            List<RecommendationResponse> policyInformationUnavailable
    ) {
        StringBuilder text = new StringBuilder("## What JanSetu can confirm\n\n")
                .append("No scheme is ready to review from the recorded evidence in your current catalogue. "
                        + "This is not a final government decision.\n\n");
        appendNextSteps(text, profileInformationNeeded, policyInformationUnavailable);

        return new ChatResult(
                text.toString(), List.of(), List.of(),
                List.of("Show my application status", "What profile information can improve matching?"), true
        );
    }

    private void appendNextSteps(
            StringBuilder text,
            List<RecommendationResponse> profileInformationNeeded,
            List<RecommendationResponse> policyInformationUnavailable
    ) {
        if (!profileInformationNeeded.isEmpty()) {
            text.append("## Complete your profile\n\n")
                    .append("JanSetu needs more information to check these recorded policies:\n");
            for (RecommendationResponse recommendation : profileInformationNeeded) {
                text.append("- **").append(recommendation.getScheme().getName()).append("** — ")
                        .append(String.join("; ", recommendation.getMissingInformation())).append("\n");
            }
            text.append("\n");
        }

        if (!policyInformationUnavailable.isEmpty()) {
            text.append("## Official policy review needed\n\n")
                    .append("JanSetu has not recorded enough policy detail to assess:\n");
            for (RecommendationResponse recommendation : policyInformationUnavailable) {
                text.append("- **").append(recommendation.getScheme().getName()).append("** — ")
                        .append("check the official source before making an application decision.\n");
            }
            text.append("\n");
        }
    }

    private Citation citationFor(SchemeResponse scheme) {
        Instant lastVerified = scheme.getLastVerifiedAt();
        return new Citation(
                scheme.getName(),
                scheme.getSourceName() == null ? "JanSetu catalogue" : scheme.getSourceName(),
                scheme.getOfficialUrl(),
                lastVerified == null ? null : lastVerified.toString()
        );
    }
}
