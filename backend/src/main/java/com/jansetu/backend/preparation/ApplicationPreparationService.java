package com.jansetu.backend.preparation;

import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationEngine;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationContext;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.profile.policy.CitizenProfilePolicyFactResolver;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionRepository;
import com.jansetu.backend.preparation.api.ApplicationPreparationResponse;
import com.jansetu.backend.preparation.api.PreparationHistoryResponse;
import com.jansetu.backend.preparation.api.PreparationTaskResponse;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluation;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ApplicationPreparationService {
    private final ApplicationPreparationSessionRepository sessions;
    private final ApplicationPreparationTaskRepository tasks;
    private final ApplicationPreparationHistoryRepository history;
    private final CurrentUserService currentUsers;
    private final CitizenProfileService profiles;
    private final SchemeRepository schemes;
    private final PolicyVersionRepository policies;
    private final EligibilityEvaluator eligibility;
    private final PolicyEvaluationEngine policyEngine;
    private final CitizenProfilePolicyFactResolver factResolver;
    private final Clock clock;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @org.springframework.beans.factory.annotation.Autowired
    public ApplicationPreparationService(ApplicationPreparationSessionRepository sessions,
                                         ApplicationPreparationTaskRepository tasks,
                                         ApplicationPreparationHistoryRepository history,
                                         CurrentUserService currentUsers,
                                         CitizenProfileService profiles,
                                         SchemeRepository schemes,
                                         PolicyVersionRepository policies,
                                         EligibilityEvaluator eligibility) {
        this(sessions, tasks, history, currentUsers, profiles, schemes, policies, eligibility, Clock.systemUTC());
    }

    ApplicationPreparationService(ApplicationPreparationSessionRepository sessions,
                                  ApplicationPreparationTaskRepository tasks,
                                  ApplicationPreparationHistoryRepository history,
                                  CurrentUserService currentUsers,
                                  CitizenProfileService profiles,
                                  SchemeRepository schemes,
                                  PolicyVersionRepository policies,
                                  EligibilityEvaluator eligibility,
                                  Clock clock) {
        this.sessions = sessions; this.tasks = tasks; this.history = history; this.currentUsers = currentUsers;
        this.profiles = profiles; this.schemes = schemes; this.policies = policies; this.eligibility = eligibility; this.clock = clock;
    }

    @Transactional
    public ApplicationPreparationResponse start(UUID schemeId) {
        Identity identity = identity();
        // Serialize starts even when the session does not exist yet.
        entityManager.lock(identity.profile(), jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        var existing = sessions.findByProfileIdAndSchemeId(identity.profile().getId(), schemeId);
        if (existing.isPresent()) return response(refreshIfNeeded(owned(existing.get().getId(), identity.profile()), identity.user()));
        Scheme scheme = schemes.findById(schemeId).filter(value -> value.getStatus() == SchemeStatus.PUBLISHED)
                .orElseThrow(() -> new SchemeNotFoundException("Published scheme not found"));
        ApplicationPreparationSession session = new ApplicationPreparationSession();
        session.setProfile(identity.profile()); session.setScheme(scheme); session.setStatus(PreparationStatus.PREPARING);
        session.setSchemeRevision(scheme.getVersion()); session.setPolicyVersion(currentPolicy(scheme.getId()).orElse(null));
        session = sessions.saveAndFlush(session);
        record(session, identity.user(), PreparationHistoryEventType.JOURNEY_STARTED, null,
                Map.of("schemeId", scheme.getId().toString()));
        rebuildTasks(session, identity.profile(), identity.user(), Set.of(), false);
        return response(session);
    }

    @Transactional
    public List<ApplicationPreparationResponse> mine() {
        Identity identity = identity();
        return sessions.findByProfileIdOrderByUpdatedAtDesc(identity.profile().getId()).stream()
                .map(session -> response(refreshIfNeeded(owned(session.getId(), identity.profile()), identity.user()))).toList();
    }

    @Transactional
    public ApplicationPreparationResponse get(UUID sessionId) {
        Identity identity = identity();
        return response(refreshIfNeeded(owned(sessionId, identity.profile()), identity.user()));
    }

    @Transactional
    public ApplicationPreparationResponse updateTask(UUID sessionId, UUID taskId, boolean complete) {
        Identity identity = identity();
        ApplicationPreparationSession session = refreshIfNeeded(owned(sessionId, identity.profile()), identity.user());
        if (session.getStatus() == PreparationStatus.CITIZEN_REPORTED_APPLICATION) {
            throw new PreparationException("Reported external applications have a frozen preparation history.");
        }
        ApplicationPreparationTask task = tasks.findByIdAndSessionId(taskId, sessionId)
                .orElseThrow(() -> new PreparationNotFoundException("Preparation task not found"));
        if (!task.isCitizenCompletable()) {
            throw new PreparationException("This task reflects recorded eligibility information. Update your profile instead.");
        }
        PreparationTaskStatus next = complete ? PreparationTaskStatus.COMPLETE : PreparationTaskStatus.INCOMPLETE;
        if (task.getStatus() != next) {
            task.setStatus(next); task.setCompletedAt(complete ? clock.instant() : null); tasks.save(task);
            record(session, identity.user(), complete ? PreparationHistoryEventType.TASK_COMPLETED : PreparationHistoryEventType.TASK_REOPENED,
                    task.getTaskKey(), Map.of("title", task.getTitle()));
            updateReadiness(session, identity.user());
        }
        return response(session);
    }

    @Transactional
    public ApplicationPreparationResponse reportApplied(UUID sessionId) {
        Identity identity = identity();
        ApplicationPreparationSession session = refreshIfNeeded(owned(sessionId, identity.profile()), identity.user());
        if (session.getStatus() != PreparationStatus.JANSETU_PREPARATION_READY) {
            throw new PreparationException("Complete every known preparation requirement before reporting an external application.");
        }
        session.setStatus(PreparationStatus.CITIZEN_REPORTED_APPLICATION);
        session.setCitizenReportedAppliedAt(clock.instant());
        record(session, identity.user(), PreparationHistoryEventType.CITIZEN_REPORTED_APPLICATION, null,
                Map.of("scope", "Citizen-reported external action; not an official government status"));
        return response(session);
    }

    private ApplicationPreparationSession refreshIfNeeded(ApplicationPreparationSession session, User actor) {
        if (session.getStatus() == PreparationStatus.CITIZEN_REPORTED_APPLICATION) return session;
        PolicyVersion current = currentPolicy(session.getScheme().getId()).orElse(null);
        UUID storedPolicy = session.getPolicyVersion() == null ? null : session.getPolicyVersion().getId();
        UUID currentPolicy = current == null ? null : current.getId();
        if (session.getSchemeRevision() == session.getScheme().getVersion() && Objects.equals(storedPolicy, currentPolicy)) return session;
        Set<String> completedDocuments = tasks.findBySessionIdOrderByTaskTypeAscTitleAsc(session.getId()).stream()
                .filter(task -> task.getTaskType() == PreparationTaskType.DOCUMENT && task.getStatus() == PreparationTaskStatus.COMPLETE)
                .map(ApplicationPreparationTask::getTaskKey).collect(java.util.stream.Collectors.toSet());
        session.setSchemeRevision(session.getScheme().getVersion()); session.setPolicyVersion(current);
        rebuildTasks(session, session.getProfile(), actor, completedDocuments, true);
        return session;
    }

    private void rebuildTasks(ApplicationPreparationSession session, CitizenProfile profile, User actor,
                              Set<String> completedDocuments, boolean refreshed) {
        tasks.deleteBySessionId(session.getId());
        tasks.flush();
        List<PreparationEvidenceReference> evidence = evidence(session);
        List<ApplicationPreparationTask> generated = new ArrayList<>();
        if (session.getScheme().getStatus() != SchemeStatus.PUBLISHED) {
            generated.add(task(session, key(PreparationTaskType.UNKNOWN_REQUIREMENT, "publication"),
                    PreparationTaskType.UNKNOWN_REQUIREMENT, PreparationTaskStatus.UNKNOWN,
                    "This scheme is not currently published",
                    "JanSetu cannot establish readiness for an unpublished or archived scheme.", false, evidence));
        }
        for (String document : session.getScheme().getRequiredDocuments()) {
            String key = key(PreparationTaskType.DOCUMENT, document);
            generated.add(task(session, key, PreparationTaskType.DOCUMENT,
                    completedDocuments.contains(key) ? PreparationTaskStatus.COMPLETE : PreparationTaskStatus.INCOMPLETE,
                    document, "Have this document ready for the official portal. Do not upload it to JanSetu.", true, evidence));
        }
        if (session.getScheme().getRequiredDocuments().isEmpty()) {
            generated.add(task(session, key(PreparationTaskType.UNKNOWN_REQUIREMENT, "documents"), PreparationTaskType.UNKNOWN_REQUIREMENT,
                    PreparationTaskStatus.UNKNOWN, "Confirm document requirements on the official portal",
                    "JanSetu has no verified document list for this scheme.", false, evidence));
        }
        if (session.getScheme().getOfficialUrl() == null || session.getScheme().getOfficialUrl().isBlank()) {
            generated.add(task(session, key(PreparationTaskType.UNKNOWN_REQUIREMENT, "official-portal"),
                    PreparationTaskType.UNKNOWN_REQUIREMENT, PreparationTaskStatus.UNKNOWN,
                    "Confirm the official application portal",
                    "No official handoff link is recorded. Verify the submission route with the authority.", false, evidence));
        }
        EligibilityEvaluation evaluation = eligibility.evaluateScheme(profile, session.getScheme());
        evaluation.matchedCriteria().forEach(value -> generated.add(task(session, key(PreparationTaskType.ELIGIBILITY, value),
                PreparationTaskType.ELIGIBILITY, PreparationTaskStatus.COMPLETE, safe(value),
                "Satisfied using your current JanSetu profile.", false, evidence)));
        evaluation.unmetCriteria().forEach(value -> generated.add(task(session, key(PreparationTaskType.ELIGIBILITY, value),
                PreparationTaskType.ELIGIBILITY, PreparationTaskStatus.INCOMPLETE, safe(value),
                "Review or correct the relevant profile information before continuing.", false, evidence)));
        evaluation.missingInformation().forEach(value -> generated.add(task(session, key(PreparationTaskType.PROFILE_FACT, value),
                PreparationTaskType.PROFILE_FACT, PreparationTaskStatus.UNKNOWN, safe(value),
                "Add the missing fact to your profile. JanSetu does not guess unavailable information.", false, evidence)));
        tasks.saveAll(generated);
        if (refreshed) record(session, actor, PreparationHistoryEventType.REQUIREMENTS_REFRESHED, null,
                Map.of("policyVersionId", session.getPolicyVersion() == null ? "none" : session.getPolicyVersion().getId().toString()));
        if (session.getStatus() != PreparationStatus.CITIZEN_REPORTED_APPLICATION) {
            session.setStatus(PreparationStatus.PREPARING); session.setReadyAt(null); updateReadiness(session, actor);
        }
    }

    private void updateReadiness(ApplicationPreparationSession session, User actor) {
        if (session.getStatus() == PreparationStatus.CITIZEN_REPORTED_APPLICATION) return;
        List<ApplicationPreparationTask> all = tasks.findBySessionIdOrderByTaskTypeAscTitleAsc(session.getId());
        boolean ready = !all.isEmpty() && all.stream().allMatch(task -> task.getStatus() == PreparationTaskStatus.COMPLETE);
        if (ready && session.getStatus() != PreparationStatus.JANSETU_PREPARATION_READY) {
            session.setStatus(PreparationStatus.JANSETU_PREPARATION_READY); session.setReadyAt(clock.instant());
            record(session, actor, PreparationHistoryEventType.PREPARATION_READY, null,
                    Map.of("scope", "JanSetu preparation only"));
        } else if (!ready) {
            session.setStatus(PreparationStatus.PREPARING); session.setReadyAt(null);
        }
    }

    private ApplicationPreparationTask task(ApplicationPreparationSession session, String key, PreparationTaskType type,
                                            PreparationTaskStatus status, String title, String guidance,
                                            boolean citizenCompletable, List<PreparationEvidenceReference> evidence) {
        ApplicationPreparationTask task = new ApplicationPreparationTask();
        task.setSession(session); task.setTaskKey(key); task.setTaskType(type); task.setStatus(status);
        task.setTitle(safe(title)); task.setGuidance(guidance); task.setCitizenCompletable(citizenCompletable);
        task.setEvidenceReferences(evidence); if (status == PreparationTaskStatus.COMPLETE) task.setCompletedAt(clock.instant());
        return task;
    }

    private List<PreparationEvidenceReference> evidence(ApplicationPreparationSession session) {
        if (session.getPolicyVersion() != null && !session.getPolicyVersion().getSourceReferences().isEmpty()) {
            return session.getPolicyVersion().getSourceReferences().stream().map(this::evidence).toList();
        }
        if (session.getScheme().getOfficialUrl() != null && !session.getScheme().getOfficialUrl().isBlank()) {
            return List.of(new PreparationEvidenceReference(null, session.getScheme().getSourceName(),
                    session.getScheme().getOfficialUrl(), null, null));
        }
        return List.of();
    }

    private PreparationEvidenceReference evidence(PolicySourceReference source) {
        return new PreparationEvidenceReference(source.snapshotId(), source.sourceTitle(), source.sourceUrl(),
                source.contentHash(), source.excerpt());
    }

    private ApplicationPreparationSession owned(UUID id, CitizenProfile profile) {
        return sessions.lockOwned(id, profile.getId())
                .orElseThrow(() -> new PreparationNotFoundException("Preparation journey not found"));
    }

    private Optional<PolicyVersion> currentPolicy(UUID schemeId) {
        return policies.findApplicableAt(schemeId, LocalDate.now(clock));
    }

    private Identity identity() {
        User user = currentUsers.getCurrentUser();
        if (user.getRole() != Role.CITIZEN || !user.isEnabled()) throw new PreparationNotFoundException("Preparation workspace not found");
        return new Identity(user, profiles.getProfileEntity(user));
    }

    private void record(ApplicationPreparationSession session, User actor, PreparationHistoryEventType type,
                        String taskKey, Map<String, String> details) {
        ApplicationPreparationHistory entry = new ApplicationPreparationHistory();
        entry.setSession(session); entry.setActor(actor); entry.setEventType(type); entry.setTaskKey(taskKey); entry.setDetails(details);
        history.save(entry);
    }

    private ApplicationPreparationResponse response(ApplicationPreparationSession session) {
        List<PreparationTaskResponse> taskResponses = tasks.findBySessionIdOrderByTaskTypeAscTitleAsc(session.getId()).stream()
                .map(PreparationTaskResponse::from).toList();
        List<PreparationHistoryResponse> historyResponses = history.findTop100BySessionIdOrderByRecordedAtDesc(session.getId()).stream()
                .map(PreparationHistoryResponse::from).toList();
        return ApplicationPreparationResponse.from(session, taskResponses, historyResponses);
    }

    private String key(PreparationTaskType type, String value) {
        return type.name().toLowerCase(Locale.ROOT) + ":" + UUID.nameUUIDFromBytes(value.trim().toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.UTF_8));
    }

    private String safe(String value) { return value.length() <= 500 ? value : value.substring(0, 500); }
    private record Identity(User user, CitizenProfile profile) { }
}
