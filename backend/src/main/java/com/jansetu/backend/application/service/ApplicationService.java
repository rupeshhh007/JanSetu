package com.jansetu.backend.application.service;

import com.jansetu.backend.application.dto.ApplicationRequest;
import com.jansetu.backend.application.dto.ApplicationResponse;
import com.jansetu.backend.application.entity.Application;
import com.jansetu.backend.application.entity.ApplicationStatusHistory;
import com.jansetu.backend.application.enums.ApplicationStatus;
import com.jansetu.backend.application.dto.ApplicationStatusHistoryResponse;
import com.jansetu.backend.application.dto.ApplicationStatusUpdateRequest;
import com.jansetu.backend.application.exception.ApplicationNotFoundException;
import com.jansetu.backend.application.exception.DuplicateApplicationException;
import com.jansetu.backend.application.exception.IneligibleException;
import com.jansetu.backend.application.exception.InvalidApplicationStatusTransitionException;
import com.jansetu.backend.application.repository.ApplicationRepository;
import com.jansetu.backend.application.repository.ApplicationStatusHistoryRepository;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository applicationStatusHistoryRepository;
    private final CitizenProfileService profileService;
    private final SchemeRepository schemeRepository;
    private final EligibilityEvaluator evaluator;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationStatusHistoryRepository applicationStatusHistoryRepository,
                              CitizenProfileService profileService,
                              SchemeRepository schemeRepository,
                              EligibilityEvaluator evaluator) {
        this.applicationRepository = applicationRepository;
        this.applicationStatusHistoryRepository = applicationStatusHistoryRepository;
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
        if (!evaluator.evaluateScheme(profile, scheme).isEligible()) {
            throw new IneligibleException("User is not eligible");
        }

        // Rejected and withdrawn applications are terminal: a citizen may submit a
        // fresh application after either. The database partial index also protects
        // this rule under concurrent requests.
        if (applicationRepository.existsByCitizenProfileAndSchemeAndStatusIn(
                profile, scheme, activeStatuses())) {
            throw new DuplicateApplicationException(
                    "An active application already exists for this scheme"
            );
        }

        // 4. Create and Save
        Application application = Application.builder()
                .citizenProfile(profile)
                .scheme(scheme)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        Application savedApp;
        try {
            savedApp = applicationRepository.saveAndFlush(application);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateApplicationException("An active application already exists for this scheme");
        }

        recordStatusChange(savedApp, null, ApplicationStatus.SUBMITTED, null, user);

        return mapToResponse(savedApp);
    }
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(User user) {
        CitizenProfile profile;
        try { profile = profileService.getProfileEntity(user); }
        catch (com.jansetu.backend.profile.exception.ProfileNotFoundException ex) { return List.of(); }

        return mapToResponses(applicationRepository.findByCitizenProfile(profile));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplicationsForAdmin() {
        return mapToResponses(applicationRepository.findAllByOrderBySubmittedAtDesc());
    }

    @Transactional
    public ApplicationResponse updateStatus(UUID applicationId, ApplicationStatusUpdateRequest request, User reviewer) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

        ApplicationStatus previousStatus = application.getStatus();
        if (!isAllowedTransition(previousStatus, request.status())) {
            throw new InvalidApplicationStatusTransitionException(
                    "Cannot change an application from " + previousStatus + " to " + request.status()
            );
        }

        application.setStatus(request.status());
        application.setRemarks(request.remarks().trim());
        Application updated = applicationRepository.save(application);
        recordStatusChange(updated, previousStatus, request.status(), request.remarks().trim(), reviewer);
        return mapToResponse(updated);
    }

    private Set<ApplicationStatus> activeStatuses() {
        return EnumSet.of(
                ApplicationStatus.DRAFT,
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.UNDER_REVIEW,
                ApplicationStatus.APPROVED
        );
    }

    private boolean isAllowedTransition(ApplicationStatus current, ApplicationStatus next) {
        return switch (current) {
            case SUBMITTED -> next == ApplicationStatus.UNDER_REVIEW || next == ApplicationStatus.REJECTED;
            case UNDER_REVIEW -> next == ApplicationStatus.APPROVED || next == ApplicationStatus.REJECTED;
            default -> false;
        };
    }

    private void recordStatusChange(
            Application application,
            ApplicationStatus previousStatus,
            ApplicationStatus newStatus,
            String remarks,
            User actor
    ) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setChangedBy(actor);
        applicationStatusHistoryRepository.save(history);
    }

    private List<ApplicationResponse> mapToResponses(List<Application> applications) {
        if (applications.isEmpty()) return List.of();
        var histories = applicationStatusHistoryRepository.findByApplicationInOrderByChangedAtDesc(applications)
                .stream().collect(java.util.stream.Collectors.groupingBy(history -> history.getApplication().getId()));
        return applications.stream().map(app -> mapToResponse(app, histories.getOrDefault(app.getId(), List.of()))).toList();
    }

    private ApplicationResponse mapToResponse(Application app) {
        return mapToResponse(app, applicationStatusHistoryRepository.findByApplicationOrderByChangedAtDesc(app));
    }

    private ApplicationResponse mapToResponse(Application app, List<ApplicationStatusHistory> historyEntries) {
        return ApplicationResponse.builder()
                .applicationId(app.getId())
                .schemeName(app.getScheme().getName())
                .schemeId(app.getScheme().getId())
                .status(app.getStatus())
                .remarks(app.getRemarks())
                .submittedAt(app.getSubmittedAt())
                .updatedAt(app.getUpdatedAt())
                .history(historyEntries.stream()
                        .map(history -> ApplicationStatusHistoryResponse.builder()
                                .id(history.getId())
                                .previousStatus(history.getPreviousStatus())
                                .newStatus(history.getNewStatus())
                                .remarks(history.getRemarks())
                                .changedBy(history.getChangedBy().getEmail())
                                .changedAt(history.getChangedAt())
                                .build())
                        .toList())
                .build();
    }
}
