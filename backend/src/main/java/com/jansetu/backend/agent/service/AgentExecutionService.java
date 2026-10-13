package com.jansetu.backend.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jansetu.backend.agent.domain.*;
import com.jansetu.backend.agent.persistence.*;
import com.jansetu.backend.agent.tool.*;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.validation.PolicyValidationIssue;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import com.jansetu.backend.source.change.SourceChangeCandidate;
import com.jansetu.backend.source.change.SourceChangeCandidateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class AgentExecutionService {
    private final PolicyAgentRunRepository runs;
    private final PolicyAgentExecutionEventRepository events;
    private final SourceChangeCandidateRepository candidates;
    private final SchemeRepository schemes;
    private final PolicyInvestigationAgent agent;
    private final AgentToolRegistry tools;
    private final AgentRunStore store;
    private final Clock clock;
    private final int maxToolCalls;
    private final Duration maxDuration;
    private final int maxModelCalls;
    private final int maxEstimatedTokens;

    @Autowired
    public AgentExecutionService(PolicyAgentRunRepository runs, PolicyAgentExecutionEventRepository events,
                                 SourceChangeCandidateRepository candidates, SchemeRepository schemes,
                                 PolicyInvestigationAgent agent, AgentToolRegistry tools, AgentRunStore store,
                                 @Value("${jansetu.agent.max-tool-calls:12}") int maxToolCalls,
                                 @Value("${jansetu.agent.max-duration-seconds:30}") long maxDurationSeconds,
                                 @Value("${jansetu.agent.max-model-calls:12}") int maxModelCalls,
                                 @Value("${jansetu.agent.max-estimated-tokens:6000}") int maxEstimatedTokens) {
        this(runs, events, candidates, schemes, agent, tools, store, Clock.systemUTC(), maxToolCalls,
                Duration.ofSeconds(maxDurationSeconds), maxModelCalls, maxEstimatedTokens);
    }

    AgentExecutionService(PolicyAgentRunRepository runs, PolicyAgentExecutionEventRepository events,
                          SourceChangeCandidateRepository candidates, SchemeRepository schemes,
                          PolicyInvestigationAgent agent, AgentToolRegistry tools, AgentRunStore store, Clock clock,
                          int maxToolCalls, Duration maxDuration, int maxModelCalls, int maxEstimatedTokens) {
        this.runs = runs;
        this.events = events;
        this.candidates = candidates;
        this.schemes = schemes;
        this.agent = agent;
        this.tools = tools;
        this.store = store;
        this.clock = clock;
        this.maxToolCalls = Math.max(1, Math.min(maxToolCalls, 20));
        this.maxDuration = maxDuration.isNegative() || maxDuration.isZero()
                ? Duration.ofSeconds(1) : maxDuration.compareTo(Duration.ofMinutes(5)) > 0
                ? Duration.ofMinutes(5) : maxDuration;
        this.maxModelCalls = Math.max(0, Math.min(maxModelCalls, 20));
        this.maxEstimatedTokens = Math.max(0, Math.min(maxEstimatedTokens, 50_000));
    }

    public PolicyAgentRun start(UUID candidateId, UUID schemeId, String idempotencyKey, User actor) {
        return start(candidateId, schemeId, AgentExecutionMode.DETERMINISTIC_FIXTURE, idempotencyKey, actor);
    }

    public PolicyAgentRun start(UUID candidateId, UUID schemeId, AgentExecutionMode mode,
                                String idempotencyKey, User actor) {
        authorize(actor);
        String key = normalizeKey(idempotencyKey);
        SourceChangeCandidate candidate = candidates.findById(candidateId)
                .orElseThrow(() -> new PolicyInvestigationException("The source change candidate does not exist."));
        Scheme scheme = schemes.findById(schemeId)
                .orElseThrow(() -> new PolicyInvestigationException("The selected scheme does not exist."));
        AgentExecutionMode resolvedMode = mode == null ? AgentExecutionMode.DETERMINISTIC_FIXTURE : mode;
        AgentRunStore.StartedRun started = store.begin(candidate, scheme, actor, resolvedMode, key,
                maxToolCalls, maxModelCalls, maxEstimatedTokens);
        if (!started.created()) return started.run();
        execute(started.run(), actor);
        return get(started.run().getId(), actor);
    }

    private void execute(PolicyAgentRun run, User actor) {
        Map<AgentToolName, AgentToolObservation> observations = agent.newObservationMap();
        PolicyInvestigationResult proposal = null;
        List<PolicyValidationIssue> validationIssues = List.of();
        try {
            while (true) {
                enforceBudgets(run);
                if (run.getExecutionMode() == AgentExecutionMode.LIVE_MODEL
                        && run.getModelCallCount() >= run.getMaxModelCalls()) {
                    throw new PolicyInvestigationException("The investigation exhausted its model-call budget.");
                }
                AgentPlanDecision plan = agent.nextTool(observations, run.getExecutionMode(),
                        run.getMaxEstimatedTokens() - run.getEstimatedTokensUsed());
                AgentToolName tool = plan.tool();
                if (tool == null) break;
                if (plan.modelCall()) {
                    run.setModelCallCount(run.getModelCallCount() + 1);
                    run.setEstimatedTokensUsed(run.getEstimatedTokensUsed() + plan.estimatedTokens());
                }
                if (tool == AgentToolName.VALIDATE_POLICY_PROPOSAL && proposal == null) {
                    proposal = agent.buildProposal(run.getId(), run.getSourceCandidate().getId(), run.getScheme().getId(),
                            observations, List.of(), run.getExecutionMode());
                }
                long started = System.nanoTime();
                AgentToolObservation observation = tools.execute(tool, run.getSourceCandidate().getId(),
                        run.getScheme().getId(), proposal, actor);
                long durationMs = Duration.ofNanos(System.nanoTime() - started).toMillis();
                observations.put(tool, observation);
                run.setToolCallCount(run.getToolCallCount() + 1);
                if (tool == AgentToolName.VALIDATE_POLICY_PROPOSAL) {
                    validationIssues = readValidationIssues(observation.value());
                    proposal = agent.buildProposal(run.getId(), run.getSourceCandidate().getId(), run.getScheme().getId(),
                            observations, validationIssues, run.getExecutionMode());
                }
                store.recordTool(run.getId(), run.getToolCallCount(), run.getModelCallCount(),
                        run.getEstimatedTokensUsed(), tool == AgentToolName.VALIDATE_POLICY_PROPOSAL
                                ? AgentEventType.VALIDATION_COMPLETED : AgentEventType.TOOL_EXECUTED,
                        tool.name(), durationMs, observation.evidenceReferences(),
                        Map.of("observationType", observation.value().getNodeType().name()));
            }
            proposal = agent.buildProposal(run.getId(), run.getSourceCandidate().getId(), run.getScheme().getId(),
                    observations, validationIssues, run.getExecutionMode());
            PolicyInvestigationResult completed = new PolicyInvestigationResult(
                    proposal.investigationId(), AgentRunStatus.COMPLETED, proposal.sourceCandidateId(), proposal.schemeId(),
                    proposal.snapshotIds(), proposal.previousPolicyVersionId(), proposal.proposedChangeSummary(),
                    proposal.proposedPolicyRule(), proposal.evidenceCitations(), proposal.unverifiedClaims(),
                    proposal.validationIssues(), true, proposal.recommendedNextAction(), proposal.deterministicFixture());
            store.complete(run.getId(), completed);
        } catch (RuntimeException failure) {
            store.fail(run.getId(), safeMessage(failure));
        }
    }

    @Transactional(readOnly = true)
    public PolicyAgentRun get(UUID runId, User actor) {
        authorize(actor);
        return runs.findDetailedById(runId)
                .orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
    }

    @Transactional(readOnly = true)
    public List<PolicyAgentRun> history(User actor) {
        authorize(actor);
        return runs.findTop50ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<PolicyAgentExecutionEvent> timeline(UUID runId, User actor) {
        get(runId, actor);
        return events.findByRunIdOrderBySequenceNumber(runId);
    }

    public PolicyAgentRun cancel(UUID runId, User actor) {
        PolicyAgentRun run = get(runId, actor);
        if (run.getStatus() != AgentRunStatus.RUNNING) {
            throw new PolicyInvestigationException("Only a running investigation can be cancelled.");
        }
        return store.cancel(runId);
    }

    public PolicyAgentRun retry(UUID runId, AgentExecutionMode requestedMode, String idempotencyKey, User actor) {
        PolicyAgentRun previous = get(runId, actor);
        if (previous.getStatus() == AgentRunStatus.RUNNING) {
            throw new PolicyInvestigationException("Cancel the running investigation before retrying it.");
        }
        AgentExecutionMode mode = requestedMode == null ? previous.getExecutionMode() : requestedMode;
        return start(previous.getSourceCandidate().getId(), previous.getScheme().getId(), mode, idempotencyKey, actor);
    }

    private List<PolicyValidationIssue> readValidationIssues(JsonNode observation) {
        List<PolicyValidationIssue> result = new ArrayList<>();
        observation.path("issues").forEach(issue -> result.add(new PolicyValidationIssue(
                issue.path("ruleId").isNull() ? null : issue.path("ruleId").asText(),
                issue.path("code").asText(), issue.path("message").asText())));
        return List.copyOf(result);
    }

    private void enforceBudgets(PolicyAgentRun run) {
        if (run.getToolCallCount() >= run.getMaxToolCalls()) {
            throw new PolicyInvestigationException("The investigation exhausted its tool-call budget.");
        }
        if (Duration.between(run.getStartedAt(), clock.instant()).compareTo(maxDuration) > 0) {
            throw new PolicyInvestigationException("The investigation exceeded its execution-time budget.");
        }
        if (run.getStatus() == AgentRunStatus.CANCELLED) {
            throw new PolicyInvestigationException("The investigation was cancelled.");
        }
    }

    private void authorize(User actor) {
        if (actor == null || actor.getId() == null || actor.getRole() != Role.ADMIN || !actor.isEnabled()) {
            throw new PolicyInvestigationException("Administrator authorization is required.");
        }
    }

    private String normalizeKey(String value) {
        String key = value == null ? "" : value.trim();
        if (key.length() < 8 || key.length() > 120) {
            throw new PolicyInvestigationException("Idempotency-Key must contain 8 to 120 characters.");
        }
        return key;
    }

    private String safeMessage(RuntimeException failure) {
        String message = failure.getMessage();
        if (!(failure instanceof PolicyInvestigationException)) {
            return "The investigation encountered an internal error. Contact an administrator to inspect server logs.";
        }
        return message == null || message.isBlank() ? "The investigation failed safely."
                : message.substring(0, Math.min(1000, message.length()));
    }
}
