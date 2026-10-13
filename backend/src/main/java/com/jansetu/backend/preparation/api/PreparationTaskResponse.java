package com.jansetu.backend.preparation.api;

import com.jansetu.backend.preparation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PreparationTaskResponse(
        UUID id, String taskKey, PreparationTaskType type, PreparationTaskStatus status,
        String title, String guidance, boolean citizenCompletable,
        List<PreparationEvidenceReference> evidenceReferences, Instant completedAt
) {
    public static PreparationTaskResponse from(ApplicationPreparationTask task) {
        return new PreparationTaskResponse(task.getId(), task.getTaskKey(), task.getTaskType(), task.getStatus(),
                task.getTitle(), task.getGuidance(), task.isCitizenCompletable(), task.getEvidenceReferences(), task.getCompletedAt());
    }
}
