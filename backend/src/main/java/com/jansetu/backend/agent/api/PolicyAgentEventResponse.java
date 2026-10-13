package com.jansetu.backend.agent.api;

import com.jansetu.backend.agent.domain.AgentEventOutcome;
import com.jansetu.backend.agent.domain.AgentEventType;
import com.jansetu.backend.agent.persistence.PolicyAgentExecutionEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PolicyAgentEventResponse(
        UUID id,
        int sequenceNumber,
        AgentEventType eventType,
        String toolName,
        AgentEventOutcome outcome,
        Long durationMs,
        List<UUID> evidenceReferences,
        Map<String, Object> details,
        Instant recordedAt
) {
    public static PolicyAgentEventResponse from(PolicyAgentExecutionEvent event) {
        return new PolicyAgentEventResponse(event.getId(), event.getSequenceNumber(), event.getEventType(),
                event.getToolName(), event.getOutcome(), event.getDurationMs(), event.getEvidenceReferences(),
                event.getDetails(), event.getRecordedAt());
    }
}
