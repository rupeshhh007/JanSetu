package com.jansetu.backend.agent.api;

import com.jansetu.backend.agent.domain.AgentExecutionMode;
import com.jansetu.backend.agent.domain.AgentRunStatus;
import com.jansetu.backend.agent.domain.PolicyInvestigationResult;
import com.jansetu.backend.agent.persistence.PolicyAgentRun;

import java.time.Instant;
import java.util.UUID;

public record PolicyAgentRunResponse(
        UUID id,
        UUID candidateId,
        UUID schemeId,
        String schemeName,
        AgentExecutionMode executionMode,
        AgentRunStatus status,
        int maxToolCalls,
        int toolCallCount,
        PolicyInvestigationResult result,
        String failureMessage,
        Instant startedAt,
        Instant completedAt,
        Instant cancelledAt
) {
    public static PolicyAgentRunResponse from(PolicyAgentRun run) {
        return new PolicyAgentRunResponse(run.getId(), run.getSourceCandidate().getId(), run.getScheme().getId(),
                run.getScheme().getName(), run.getExecutionMode(), run.getStatus(), run.getMaxToolCalls(),
                run.getToolCallCount(), run.getResult(), run.getFailureMessage(), run.getStartedAt(),
                run.getCompletedAt(), run.getCancelledAt());
    }
}
