package com.jansetu.backend.ai.recommendation;

import com.jansetu.backend.application.entity.Application;
import com.jansetu.backend.application.repository.ApplicationRepository;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.service.MatchingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.jansetu.backend.auth.service.CurrentUserService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationAIService {

    private final MatchingService matchingService;

    private final CitizenProfileService profileService;

    private final ApplicationRepository applicationRepository;

    private final ChatClient chatClient;
    private final CurrentUserService currentUserService;


    public RecommendationAIService(
            MatchingService matchingService,
            CitizenProfileService profileService,
            ApplicationRepository applicationRepository,
            CurrentUserService currentUserService,
            ChatClient.Builder builder
    ) {

        this.matchingService = matchingService;
        this.profileService = profileService;
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
        this.chatClient = builder.build();
    }

    public String generateRecommendation() {
        User user = currentUserService.getCurrentUser();

        CitizenProfile profile =
                profileService.getProfileEntity(user);

        List<RecommendationResponse> recommendations =
                matchingService.getRecommendations(user);

        List<Application> applications =
                applicationRepository.findByCitizenProfile(profile);

        Set<UUID> appliedSchemeIds =
                applications.stream()
                        .map(application -> application.getScheme().getId())
                        .collect(Collectors.toSet());

        List<RecommendationResponse> availableSchemes =
                recommendations.stream()
                        .filter(recommendation ->
                                !appliedSchemeIds.contains(
                                        recommendation.getScheme().getId()
                                )
                        )
                        .toList();

        if (availableSchemes.isEmpty()) {

            return """
You have already applied for every scheme you are currently eligible for.

Please wait for your existing applications to be reviewed before applying again.
""";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
The citizen can still apply for ONLY the following schemes.

Never mention any other scheme.

Never invent schemes.

Explain why these schemes are good choices.

==================================================

""");

        for (RecommendationResponse recommendation : availableSchemes) {

            builder.append("Scheme: ")
                    .append(recommendation.getScheme().getName())
                    .append("\n");

            builder.append("Description: ")
                    .append(recommendation.getScheme().getDescription())
                    .append("\n");

            builder.append("Match Percentage: ")
                    .append(recommendation.getMatchPercentage())
                    .append("%\n");

            builder.append("Reasons:\n");

            for (String reason : recommendation.getMatchedCriteria()) {

                builder.append("- ")
                        .append(reason)
                        .append("\n");
            }

            builder.append("\n--------------------------------------\n\n");
        }

        builder.append("""
Explain which scheme should be applied for first.

Rank them in priority order.

Keep the answer under 200 words.

Use markdown.

Never mention schemes that are not listed above.
""");

        return chatClient
                .prompt()
                .system("""
You are JanSetu AI.

You ONLY explain recommendations.

Never invent schemes.

Never invent benefits.

Never recommend schemes outside the supplied list.
""")
                .user(builder.toString())
                .call()
                .content();
    }
}