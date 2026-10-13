package com.jansetu.backend.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.agent.domain.EvidenceCitation;
import com.jansetu.backend.agent.domain.PolicyInvestigationResult;
import com.jansetu.backend.agent.domain.AgentRunStatus;
import com.jansetu.backend.agent.tool.AgentToolName;
import com.jansetu.backend.agent.tool.AgentToolObservation;
import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.validation.PolicyValidationIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Deterministic planner used by CI and offline demos. Every step still executes a real allowlisted tool. */
@Component
public class PolicyInvestigationAgent {
    private static final List<AgentToolName> PLAN = List.of(
            AgentToolName.GET_SOURCE_CHANGE_CANDIDATE,
            AgentToolName.GET_PREVIOUS_SOURCE_SNAPSHOT,
            AgentToolName.GET_NEW_SOURCE_SNAPSHOT,
            AgentToolName.GET_SCHEME_DETAILS,
            AgentToolName.GET_CURRENT_POLICY_VERSION,
            AgentToolName.GET_POLICY_VERSION_HISTORY,
            AgentToolName.COMPARE_POLICY_EVIDENCE,
            AgentToolName.VALIDATE_POLICY_PROPOSAL,
            AgentToolName.IDENTIFY_MISSING_EVIDENCE,
            AgentToolName.SAVE_INVESTIGATION_DRAFT
    );

    private final ObjectMapper json;

    public PolicyInvestigationAgent(ObjectMapper json) {
        this.json = json;
    }

    public AgentToolName nextTool(Map<AgentToolName, AgentToolObservation> observations) {
        return PLAN.stream().filter(tool -> !observations.containsKey(tool)).findFirst().orElse(null);
    }

    public PolicyInvestigationResult buildProposal(UUID runId, UUID candidateId, UUID schemeId,
                                                   Map<AgentToolName, AgentToolObservation> observations,
                                                   List<PolicyValidationIssue> validationIssues) {
        JsonNode candidate = required(observations, AgentToolName.GET_SOURCE_CHANGE_CANDIDATE);
        JsonNode previous = required(observations, AgentToolName.GET_PREVIOUS_SOURCE_SNAPSHOT);
        JsonNode current = required(observations, AgentToolName.GET_CURRENT_POLICY_VERSION);
        JsonNode newer = required(observations, AgentToolName.GET_NEW_SOURCE_SNAPSHOT);
        PolicyRule rule = current.isNull() || current.get("policyRule") == null
                ? null : json.convertValue(current.get("policyRule"), PolicyRule.class);
        UUID previousPolicyId = current.isNull() ? null : UUID.fromString(current.get("policyVersionId").asText());
        String exactSpan = boundedCitation(newer.path("evidence").asText());
        List<String> unknowns = new ArrayList<>();
        unknowns.add("The detected text change requires an administrator to confirm legal meaning and effective date.");
        if (rule == null) unknowns.add("No published typed policy exists to use as the proposed AST baseline.");
        return new PolicyInvestigationResult(
                runId,
                AgentRunStatus.RUNNING,
                candidateId,
                schemeId,
                List.of(UUID.fromString(previous.get("snapshotId").asText()),
                        UUID.fromString(newer.get("snapshotId").asText())),
                previousPolicyId,
                candidate.path("changeSummary").asText(),
                rule,
                exactSpan.isBlank() ? List.of() : List.of(new EvidenceCitation(
                        UUID.fromString(newer.get("snapshotId").asText()),
                        newer.path("sourceTitle").asText(),
                        newer.path("sourceUrl").asText(),
                        newer.path("contentHash").asText(),
                        exactSpan)),
                unknowns,
                validationIssues,
                true,
                "Administrator must inspect the cited evidence, edit the typed draft if needed, and use the existing review workflow.",
                true
        );
    }

    public Map<AgentToolName, AgentToolObservation> newObservationMap() {
        return new EnumMap<>(AgentToolName.class);
    }

    private JsonNode required(Map<AgentToolName, AgentToolObservation> observations, AgentToolName tool) {
        AgentToolObservation observation = observations.get(tool);
        if (observation == null) throw new PolicyInvestigationException("Required observation is missing: " + tool);
        return observation.value();
    }

    private String boundedCitation(String evidence) {
        String trimmed = evidence == null ? "" : evidence.strip();
        return trimmed.length() <= 1_000 ? trimmed : trimmed.substring(0, 1_000);
    }
}
