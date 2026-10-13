package com.jansetu.backend.preparation.api;

import com.jansetu.backend.preparation.ApplicationPreparationSession;
import com.jansetu.backend.preparation.PreparationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApplicationPreparationResponse(
        UUID id, UUID schemeId, String schemeName, String officialUrl, String sourceName,
        PreparationStatus status, String officialApplicationStatus,
        UUID policyVersionId, Integer policyVersionNumber,
        int completedTasks, int totalTasks, int progressPercent,
        List<PreparationTaskResponse> tasks, List<PreparationHistoryResponse> history,
        Instant readyAt, Instant citizenReportedAppliedAt, Instant createdAt, Instant updatedAt
) {
    public static ApplicationPreparationResponse from(ApplicationPreparationSession session,
                                                       List<PreparationTaskResponse> tasks,
                                                       List<PreparationHistoryResponse> history) {
        int completed = (int) tasks.stream().filter(task -> task.status() == com.jansetu.backend.preparation.PreparationTaskStatus.COMPLETE).count();
        int percent = tasks.isEmpty() ? 0 : completed * 100 / tasks.size();
        return new ApplicationPreparationResponse(session.getId(), session.getScheme().getId(), session.getScheme().getName(),
                session.getScheme().getOfficialUrl(), session.getScheme().getSourceName(), session.getStatus(), null,
                session.getPolicyVersion() == null ? null : session.getPolicyVersion().getId(),
                session.getPolicyVersion() == null ? null : session.getPolicyVersion().getVersionNumber(),
                completed, tasks.size(), percent, tasks, history, session.getReadyAt(),
                session.getCitizenReportedAppliedAt(), session.getCreatedAt(), session.getUpdatedAt());
    }
}
