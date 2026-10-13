package com.jansetu.backend.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record AgentToolObservation(JsonNode value, List<UUID> evidenceReferences) {
    public AgentToolObservation {
        evidenceReferences = List.copyOf(evidenceReferences == null ? List.of() : evidenceReferences);
    }
}
