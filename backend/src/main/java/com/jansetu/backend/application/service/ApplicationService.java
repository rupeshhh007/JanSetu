package com.jansetu.backend.application.service;

import com.jansetu.backend.application.dto.ApplicationRequest;
import com.jansetu.backend.application.dto.ApplicationResponse;
import com.jansetu.backend.application.entity.Application;
import com.jansetu.backend.application.enums.ApplicationStatus;
import com.jansetu.backend.application.exception.DuplicateApplicationException;
import com.jansetu.backend.application.exception.IneligibleException;
import com.jansetu.backend.application.repository.ApplicationRepository;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CitizenProfileService profileService;
    private final SchemeRepository schemeRepository;
    private final EligibilityEvaluator evaluator;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CitizenProfileService profileService,
                              SchemeRepository schemeRepository,
                              EligibilityEvaluator evaluator) {
        this.applicationRepository = applicationRepository;
        this.profileService = profileService;
        this.schemeRepository = schemeRepository;
        this.evaluator = evaluator;
    }

    @Transactional
    public ApplicationResponse submitApplication(User user, ApplicationRequest request) {
        // 1. Fetch Profile and Scheme
        CitizenProfile profile = profileService.getProfileEntity(user);
        Scheme scheme = schemeRepository.findById(request.getSchemeId())
                .orElseThrow(() -> new SchemeNotFoundException("Scheme not found"));

        // 2. Validate Eligibility
        if (!evaluator.isEligible(profile, scheme.getEligibilityCriteria())) {
            throw new IneligibleException("User is not eligible");
        }

        // 3. Validate Duplicates (Exclude Rejected/Withdrawn)
        if (applicationRepository.existsByCitizenProfileAndSchemeAndStatusNot(
                profile, scheme, ApplicationStatus.REJECTED)) { // Simplification: adjust logic as needed
            throw new DuplicateApplicationException(
                    "An active application already exists for this scheme"
            );        }

        // 4. Create and Save
        Application application = Application.builder()
                .citizenProfile(profile)
                .scheme(scheme)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        Application savedApp = applicationRepository.save(application);

        return mapToResponse(savedApp);
    }
    public List<ApplicationResponse> getMyApplications(User user) {
        CitizenProfile profile = profileService.getProfileEntity(user);

        return applicationRepository.findByCitizenProfile(profile).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ApplicationResponse mapToResponse(Application app) {
        return ApplicationResponse.builder()
                .applicationId(app.getId())
                .schemeName(app.getScheme().getName())
                .schemeId(app.getScheme().getId())
                .status(app.getStatus())
                .remarks(app.getRemarks())
                .submittedAt(app.getSubmittedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}