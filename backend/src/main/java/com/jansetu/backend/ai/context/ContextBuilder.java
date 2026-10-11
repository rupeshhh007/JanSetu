package com.jansetu.backend.ai.context;

import com.jansetu.backend.application.entity.Application;
import com.jansetu.backend.application.repository.ApplicationRepository;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.dto.RecommendationResponse;
import com.jansetu.backend.scheme.service.MatchingService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContextBuilder {

    private final CurrentUserService currentUserService;
    private final CitizenProfileService profileService;
    private final MatchingService matchingService;
    private final ApplicationRepository applicationRepository;

    public ContextBuilder(
            CurrentUserService currentUserService,
            CitizenProfileService profileService,
            MatchingService matchingService,
            ApplicationRepository applicationRepository
    ) {
        this.currentUserService = currentUserService;
        this.profileService = profileService;
        this.matchingService = matchingService;
        this.applicationRepository = applicationRepository;
    }

    public UserContext buildContext() {

        User currentUser = currentUserService.getCurrentUser();

        CitizenProfile profile;
        try {
            profile = profileService.getProfileEntity(currentUser);
        } catch (com.jansetu.backend.profile.exception.ProfileNotFoundException ex) {
            return new UserContext("Citizen profile has not been created.",
                    "Create a profile to receive personalized matches.", "No applications yet. Create your profile to begin.");
        }

        String profileSummary =
                buildProfileSummary(profile);

        String recommendationSummary =
                buildRecommendationSummary(currentUser);

        String applicationSummary =
                buildApplicationSummary(profile);

        return new UserContext(
                profileSummary,
                recommendationSummary,
                applicationSummary
        );
    }

    private String buildProfileSummary(CitizenProfile profile) {
        // This text can leave the application boundary when an optional model
        // provider is enabled.  Keep it intentionally minimal: the server-side
        // evaluator has already used sensitive profile fields to make its
        // decision, so the model does not need a name, date of birth, income,
        // caste/category, disability, or district.
        return """
PRIVACY-MINIMIZED CITIZEN CONTEXT

The citizen's eligibility was calculated on the server from their private
profile. Do not infer or request sensitive attributes. Their recorded state is:
%s.
"""
                .formatted(
                        profile.getState()
                );
    }

    private String buildRecommendationSummary(User user) {

        List<RecommendationResponse> recommendations =
                matchingService.getRecommendations(user);

        if (recommendations.isEmpty()) {
            return """
========================
RECORDED RECOMMENDATION EVIDENCE
========================

No schemes have recorded recommendation evidence for this profile.
""";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
========================
RECORDED RECOMMENDATION EVIDENCE
========================

""");

        int index = 1;

        for (RecommendationResponse recommendation : recommendations) {

            builder.append(index++)
                    .append(". ")
                    .append(recommendation.getScheme().getName())
                    .append("\n");

            builder.append("State: ")
                    .append(recommendation.getRecommendationState())
                    .append("\n")
                    .append("Recorded evidence: ")
                    .append(recommendation.getEvidenceScore())
                    .append("% across ")
                    .append(recommendation.getRecordedRuleCount())
                    .append(" recorded rules\n");

            builder.append("Recorded details:\n");

            for (String reason : recommendation.getMatchedCriteria()) {

                builder.append("- ")
                        .append(reason)
                        .append("\n");
            }

            for (String detail : recommendation.getMissingInformation()) {
                builder.append("- Information needed: ")
                        .append(detail)
                        .append("\n");
            }

            for (String detail : recommendation.getUnmetCriteria()) {
                builder.append("- Not supported by the recorded profile: ")
                        .append(detail)
                        .append("\n");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    private String buildApplicationSummary(CitizenProfile profile) {
        List<Application> applications = applicationRepository.findByCitizenProfile(profile);
        if (applications.isEmpty()) return "You have not created any JanSetu tracking records yet. Explore a scheme to begin.";
        StringBuilder builder = new StringBuilder("These are JanSetu tracking records, not government application decisions.\n\n");
        for (Application application : applications) {
            builder.append("### ").append(application.getScheme().getName()).append("\n")
                    .append("- **Status:** ").append(application.getStatus().name().replace('_', ' ')).append("\n");
            if (application.getRemarks() != null && !application.getRemarks().isBlank()) {
                builder.append("- **Review note:** ").append(application.getRemarks()).append("\n");
            }
            builder.append("- **Next step:** ").append(switch (application.getStatus()) {
                case SUBMITTED, UNDER_REVIEW -> "Check your applications page for the next review update.";
                case APPROVED -> "Review the note and continue through the official government portal.";
                case REJECTED -> "Read the review note. Update your profile if needed before creating another record.";
                case WITHDRAWN -> "You can create a new record if you meet the recorded requirements.";
                default -> "Review the latest details in your applications page.";
            }).append("\n\n");
        }
        return builder.toString();
    }
}
