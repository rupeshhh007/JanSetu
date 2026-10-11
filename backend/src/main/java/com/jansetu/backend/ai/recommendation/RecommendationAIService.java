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

        List<RecommendationResponse> available = recommendations.stream()
                .filter(recommendation -> !appliedSchemeIds.contains(recommendation.getScheme().getId()))
                .sorted(Comparator.comparing(item -> item.getScheme().getName()))
                .toList();

        if (available.isEmpty()) {
            return new ChatResult(
                    "I couldn’t find a new scheme that your saved profile satisfies in the current JanSetu catalogue. "
                            + "This is not a final government decision. Update incomplete profile details or check existing applications.",
                    List.of(), List.of(),
                    List.of("Show my application status", "What profile information can improve matching?"),
                    true
            );
        }

        StringBuilder text = new StringBuilder("## Your eligible schemes\n\n")
                .append("These results use the eligibility rules recorded in JanSetu. "
                        + "Confirm final eligibility on the official source before applying.\n\n");
        List<Citation> citations = new ArrayList<>();
        List<ChatCard> cards = new ArrayList<>();

        for (int index = 0; index < available.size(); index++) {
            RecommendationResponse recommendation = available.get(index);
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

        return new ChatResult(
                text.toString(), List.copyOf(citations), List.copyOf(cards),
                List.of("What documents are recorded for these schemes?", "Compare these schemes"), true
        );
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
