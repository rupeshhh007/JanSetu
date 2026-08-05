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

import java.time.LocalDate;
import java.time.Period;
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

        CitizenProfile profile =
                profileService.getProfileEntity(currentUser);

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

        int age = Period.between(
                profile.getDateOfBirth(),
                LocalDate.now()
        ).getYears();

        return """
========================
CITIZEN PROFILE
========================

Name: %s %s

Age: %d

Gender: %s

Occupation: %s

Category: %s

Annual Family Income: ₹%s

State: %s

District: %s

Student: %s

Farmer: %s

Disability: %s
"""
                .formatted(
                        profile.getFirstName(),
                        profile.getLastName(),
                        age,
                        profile.getGender(),
                        profile.getPrimaryOccupation(),
                        profile.getCategory(),
                        profile.getAnnualFamilyIncome().toPlainString(),
                        profile.getState(),
                        profile.getDistrict(),
                        profile.isStudent() ? "Yes" : "No",
                        profile.isFarmer() ? "Yes" : "No",
                        profile.isHasDisability() ? "Yes" : "No"
                );
    }

    private String buildRecommendationSummary(User user) {

        List<RecommendationResponse> recommendations =
                matchingService.getRecommendations(user);

        if (recommendations.isEmpty()) {
            return """
========================
ELIGIBLE SCHEMES
========================

No eligible schemes found.
""";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
========================
ELIGIBLE SCHEMES
========================

""");

        int index = 1;

        for (RecommendationResponse recommendation : recommendations) {

            builder.append(index++)
                    .append(". ")
                    .append(recommendation.getScheme().getName())
                    .append("\n");

            builder.append("Match: ")
                    .append(recommendation.getMatchPercentage())
                    .append("%\n");

            builder.append("Reasons:\n");

            for (String reason : recommendation.getMatchedCriteria()) {

                builder.append("- ")
                        .append(reason)
                        .append("\n");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    private String buildApplicationSummary(
            CitizenProfile profile
    ) {

        List<Application> applications =
                applicationRepository.findByCitizenProfile(profile);

        if (applications.isEmpty()) {

            return """
========================
APPLICATION HISTORY
========================

The citizen has not applied for any schemes yet.

IMPORTANT FOR AI

• All eligible schemes are available for application.
• You may recommend eligible schemes.
""";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
========================
APPLICATION HISTORY
========================

The following schemes ALREADY have an application.

IMPORTANT FOR AI

• NEVER recommend applying again to these schemes.
• Instead, inform the citizen about their application status.
• Recommend ONLY eligible schemes that are NOT listed below.

--------------------------------------------------

""");

        for (Application application : applications) {

            builder.append("Scheme: ")
                    .append(application.getScheme().getName())
                    .append("\n");

            builder.append("Status: ")
                    .append(application.getStatus())
                    .append("\n");

            switch (application.getStatus()) {

                case SUBMITTED ->
                        builder.append("""
Action Required:
Wait for review.
""");

                case APPROVED ->
                        builder.append("""
Action Required:
You have already been approved.
No further application is needed.
""");

                case REJECTED ->
                        builder.append("""
Action Required:
You may check the rejection reason before applying again.
""");

                case UNDER_REVIEW ->
                        builder.append("""
Action Required:
Your application is currently under review.
""");

                case WITHDRAWN ->
                        builder.append("""
Action Required:
This application was withdrawn.
You may submit a fresh application if eligible.
""");

                default ->
                        builder.append("""
Action Required:
Check the latest application status.
""");
            }

            builder.append("\n----------------------------------------\n\n");
        }

        return builder.toString();
    }

}