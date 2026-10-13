package com.jansetu.backend.preparation.api;

import com.jansetu.backend.preparation.ApplicationPreparationHistory;
import com.jansetu.backend.preparation.PreparationHistoryEventType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PreparationHistoryResponse(UUID id, PreparationHistoryEventType eventType, String taskKey,
                                         String actorEmail, Map<String, String> details, Instant recordedAt) {
    public static PreparationHistoryResponse from(ApplicationPreparationHistory history) {
        return new PreparationHistoryResponse(history.getId(), history.getEventType(), history.getTaskKey(),
                history.getActor().getEmail(), history.getDetails(), history.getRecordedAt());
    }
}
