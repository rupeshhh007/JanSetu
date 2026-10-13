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
    private final Clock clock;
    private final int maxToolCalls;
    private final Duration maxDuration;

    @Autowired
    public AgentExecutionService(PolicyAgentRunRepository runs, PolicyAgentExecutionEventRepository events,
                                 SourceChangeCandidateRepository candidates, SchemeRepository schemes,
                                 PolicyInvestigationAgent agent, AgentToolRegistry tools,
                                 @Value("${jansetu.agent.max-tool-calls:12}") int maxToolCalls,
                                 @Value("${jansetu.agent.max-duration-seconds:30}") long maxDurationSeconds) {
        this(runs, events, candidates, schemes, agent, tools, Clock.systemUTC(), maxToolCalls,
                Duration.ofSeconds(maxDurationSeconds));
    }

    AgentExecutionService(PolicyAgentRunRepository runs, PolicyAgentExecutionEventRepository events,
                          SourceChangeCandidateRepository candidates, SchemeRepository schemes,
                          PolicyInvestigationAgent agent, AgentToolRegistry tools, Clock clock,
                          int maxToolCalls, Duration maxDuration) {
        this.runs = runs;
        this.events = events;
        this.candidates = candidates;
        this.schemes = schemes;
        this.agent = agent;
        this.tools = tools;
        this.clock = clock;
        this.maxToolCalls = Math.max(1, Math.min(maxToolCalls, 20));
        this.maxDuration = maxDuration;
    }

    @Transactional
    public PolicyAgentRun start(UUID candidateId, UUID schemeId, String idempotencyKey, User actor) {
        authorize(actor);
        String key = normalizeKey(idempotencyKey);
        Optional<PolicyAgentRun> existing = runs.findByStartedByIdAndIdempotencyKey(actor.getId(), key);
        if (existing.isPresent()) return existing.get();

        SourceChangeCandidate candidate = candidates.findById(candidateId)
                .orElseThrow(() -> new PolicyInvestigationException("The source change candidate does not exist."));
        Scheme scheme = schemes.findById(schemeId)
                .orElseThrow(() -> new PolicyInvestigationException("The selected scheme does not exist."));
        PolicyAgentRun run = new PolicyAgentRun();
        run.setSourceCandidate(candidate);
        run.setScheme(scheme);
        run.setStartedBy(actor);
        run.setExecutionMode(AgentExecutionMode.DETERMINISTIC_FIXTURE);
        run.setStatus(AgentRunStatus.RUNNING);
        run.setIdempotencyKey(key);
        run.setMaxToolCalls(maxToolCalls);
        run.setStartedAt(clock.instant());
        run = runs.saveAndFlush(run);
        append(run, AgentEventType.RUN_STARTED, null, AgentEventOutcome.SUCCESS, null, List.of(),
                Map.of("mode", run.getExecutionMode().name(), "maxToolCalls", maxToolCalls));
        execute(run, actor);
        return run;
    }

    private void execute(PolicyAgentRun run, User actor) {
        Map<AgentToolName, AgentToolObservation> observations = agent.newObservationMap();
        PolicyInvestigationResult proposal = null;
        List<PolicyValidationIssue> validationIssues = List.of();
        try {
            while (true) {
                enforceBudgets(run);
                AgentToolName tool = agent.nextTool(observations);
                if (tool == null) break;
                if (tool == AgentToolName.VALIDATE_POLICY_PROPOSAL && proposal == null) {
                    proposal = agent.buildProposal(run.getId(), run.getSourceCandidate().getId(), run.getScheme().getId(),
                            observations, List.of());
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
                            observations, validationIssues);
                }
                append(run, tool == AgentToolName.VALIDATE_POLICY_PROPOSAL
                                ? AgentEventType.VALIDATION_COMPLETED : AgentEventType.TOOL_EXECUTED,
                        tool.name(), AgentEventOutcome.SUCCESS, durationMs, observation.evidenceReferences(),
                        Map.of("observationType", observation.value().getNodeType().name()));
            }
            proposal = agent.buildProposal(run.getId(), run.getSourceCandidate().getId(), run.getScheme().getId(),
                    observations, validationIssues);
            PolicyInvestigationResult completed = new PolicyInvestigationResult(
                    proposal.investigationId(), AgentRunStatus.COMPLETED, proposal.sourceCandidateId(), proposal.schemeId(),
                    proposal.snapshotIds(), proposal.previousPolicyVersionId(), proposal.proposedChangeSummary(),
                    proposal.proposedPolicyRule(), proposal.evidenceCitations(), proposal.unverifiedClaims(),
                    proposal.validationIssues(), true, proposal.recommendedNextAction(), proposal.deterministicFixture());
            run.setResult(completed);
            run.setStatus(AgentRunStatus.COMPLETED);
            run.setCompletedAt(clock.instant());
            append(run, AgentEventType.RUN_COMPLETED, null, AgentEventOutcome.SUCCESS, null,
                    completed.snapshotIds(), Map.of("valid", completed.validationIssues().isEmpty(),
                            "publicationPerformed", false));
        } catch (RuntimeException failure) {
            run.setStatus(AgentRunStatus.FAILED);
            run.setFailureMessage(safeMessage(failure));
            run.setCompletedAt(clock.instant());
            append(run, AgentEventType.RUN_FAILED, null, AgentEventOutcome.ERROR, null, List.of(),
                    Map.of("message", safeMessage(failure)));
        }
        runs.save(run);
    }

    @Transactional(readOnly = true)
    public PolicyAgentRun get(UUID runId, User actor) {
        authorize(actor);
        return runs.findById(runId).orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
    }

    @Transactional(readOnly = true)
    public List<PolicyAgentRun> history(User actor) {
        authorize(actor);
        return runs.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<PolicyAgentExecutionEvent> timeline(UUID runId, User actor) {
        get(runId, actor);
        return events.findByRunIdOrderBySequenceNumber(runId);
    }

    @Transactional
    public PolicyAgentRun cancel(UUID runId, User actor) {
        PolicyAgentRun run = get(runId, actor);
        if (run.getStatus() != AgentRunStatus.RUNNING) {
            throw new PolicyInvestigationException("Only a running investigation can be cancelled.");
        }
        run.setStatus(AgentRunStatus.CANCELLED);
        run.setCancelledAt(clock.instant());
        append(run, AgentEventType.RUN_CANCELLED, null, AgentEventOutcome.SUCCESS, null, List.of(), Map.of());
        return runs.save(run);
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

    private void append(PolicyAgentRun run, AgentEventType type, String toolName, AgentEventOutcome outcome,
                        Long durationMs, List<UUID> evidence, Map<String, Object> details) {
        PolicyAgentExecutionEvent event = new PolicyAgentExecutionEvent();
        event.setRun(run);
        event.setSequenceNumber((int) events.countByRunId(run.getId()) + 1);
        event.setEventType(type);
        event.setToolName(toolName);
        event.setOutcome(outcome);
        event.setDurationMs(durationMs);
        event.setEvidenceReferences(evidence);
        event.setDetails(details);
        event.setRecordedAt(clock.instant());
        events.save(event);
    }

    private void authorize(User actor) {
        if (actor == null || actor.getRole() != Role.ADMIN || !actor.isEnabled()) {
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
        return message == null || message.isBlank() ? "The investigation failed safely." : message.substring(0, Math.min(1000, message.length()));
    }
}
