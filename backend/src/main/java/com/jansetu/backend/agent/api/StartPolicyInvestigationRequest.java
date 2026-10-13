package com.jansetu.backend.agent.api;

import jakarta.validation.constraints.NotNull;
import com.jansetu.backend.agent.domain.AgentExecutionMode;

import java.util.UUID;

public record StartPolicyInvestigationRequest(
        @NotNull UUID candidateId,
        @NotNull UUID schemeId,
        AgentExecutionMode executionMode
) {
    public StartPolicyInvestigationRequest(UUID candidateId, UUID schemeId) {
        this(candidateId, schemeId, AgentExecutionMode.DETERMINISTIC_FIXTURE);
    }

    public AgentExecutionMode resolvedMode() {
        return executionMode == null ? AgentExecutionMode.DETERMINISTIC_FIXTURE : executionMode;
    }
}
