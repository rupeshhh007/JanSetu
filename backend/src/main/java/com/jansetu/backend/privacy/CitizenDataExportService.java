package com.jansetu.backend.privacy;

import com.jansetu.backend.application.service.ApplicationService;
import com.jansetu.backend.assessment.PolicyAssessmentService;
import com.jansetu.backend.assessment.api.PolicyAssessmentResponse;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.bookmark.CitizenSavedSchemeService;
import com.jansetu.backend.notification.CitizenPolicyNotificationService;
import com.jansetu.backend.notification.api.CitizenPolicyNotificationResponse;
import com.jansetu.backend.preparation.ApplicationPreparationService;
import com.jansetu.backend.privacy.api.CitizenDataExportResponse;
import com.jansetu.backend.profile.dto.ProfileFactResponse;
import com.jansetu.backend.profile.dto.ProfileResponse;
import com.jansetu.backend.profile.exception.ProfileNotFoundException;
import com.jansetu.backend.profile.fact.CitizenProfileFactService;
import com.jansetu.backend.profile.service.CitizenProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Only the currently authenticated account can export its own citizen records. */
@Service
public class CitizenDataExportService {
    private final CurrentUserService current;
    private final CitizenProfileService profiles;
    private final CitizenProfileFactService facts;
    private final CitizenSavedSchemeService bookmarks;
    private final ApplicationService applications;
    private final ApplicationPreparationService preparations;
    private final PolicyAssessmentService assessments;
    private final CitizenPolicyNotificationService notifications;

    public CitizenDataExportService(CurrentUserService current, CitizenProfileService profiles,
                                    CitizenProfileFactService facts, CitizenSavedSchemeService bookmarks,
                                    ApplicationService applications, ApplicationPreparationService preparations,
                                    PolicyAssessmentService assessments, CitizenPolicyNotificationService notifications) {
        this.current = current;
        this.profiles = profiles;
        this.facts = facts;
        this.bookmarks = bookmarks;
        this.applications = applications;
        this.preparations = preparations;
        this.assessments = assessments;
        this.notifications = notifications;
    }

    @Transactional
    public CitizenDataExportResponse exportMine() {
        User citizen = current.getCurrentUser();
        ProfileResponse profile;
        try {
            profile = profiles.getProfile(citizen);
        } catch (ProfileNotFoundException missingProfile) {
            profile = null;
        }

        var recordedFacts = profile == null ? List.<ProfileFactResponse>of()
                : facts.factsForCitizen(citizen).stream()
                    .map(fact -> ProfileFactResponse.from(fact, facts.isActive(fact))).toList();
        var preparationHistory = profile == null
                ? List.<com.jansetu.backend.preparation.api.ApplicationPreparationResponse>of()
                : preparations.mine();
        var policyAssessments = profile == null ? List.<PolicyAssessmentResponse>of()
                : assessments.history(citizen).stream().map(PolicyAssessmentResponse::from).toList();

        return new CitizenDataExportResponse("1", Instant.now(), profile, recordedFacts,
                bookmarks.mine(), applications.getMyApplications(citizen),
                preparationHistory, policyAssessments,
                notifications.inbox(citizen).stream().map(CitizenPolicyNotificationResponse::from).toList());
    }
}
