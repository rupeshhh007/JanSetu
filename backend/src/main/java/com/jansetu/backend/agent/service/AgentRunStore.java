package com.jansetu.backend.agent.service;

import com.jansetu.backend.agent.domain.*;
import com.jansetu.backend.agent.persistence.*;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.source.change.SourceChangeCandidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persists checkpoints in independent transactions so model/network work never holds a database transaction open. */
@Service
public class AgentRunStore {
    private final PolicyAgentRunRepository runs;
    private final PolicyAgentExecutionEventRepository events;
    private final Clock clock = Clock.systemUTC();

    public AgentRunStore(PolicyAgentRunRepository runs, PolicyAgentExecutionEventRepository events) {
        this.runs = runs;
        this.events = events;
    }

    @Transactional
    public StartedRun begin(SourceChangeCandidate candidate, Scheme scheme, User actor, AgentExecutionMode mode,
                            String key, int maxToolCalls, int maxModelCalls, int maxEstimatedTokens) {
        var existing = runs.findByStartedByIdAndIdempotencyKey(actor.getId(), key);
        if (existing.isPresent()) {
            PolicyAgentRun prior = existing.get();
            requireMatchingScope(prior, candidate, scheme, mode);
            return new StartedRun(prior, false);
        }
        PolicyAgentRun run = new PolicyAgentRun();
        run.setSourceCandidate(candidate); run.setScheme(scheme); run.setStartedBy(actor); run.setExecutionMode(mode);
        run.setStatus(AgentRunStatus.RUNNING); run.setIdempotencyKey(key); run.setMaxToolCalls(maxToolCalls);
        run.setMaxModelCalls(mode == AgentExecutionMode.LIVE_MODEL ? maxModelCalls : 0);
        run.setMaxEstimatedTokens(mode == AgentExecutionMode.LIVE_MODEL ? maxEstimatedTokens : 0);
        run.setStartedAt(clock.instant());
        run = runs.saveAndFlush(run);
        append(run, AgentEventType.RUN_STARTED, null, AgentEventOutcome.SUCCESS, null, List.of(),
                Map.of("mode", mode.name(), "maxToolCalls", maxToolCalls,
                        "maxModelCalls", run.getMaxModelCalls(), "maxEstimatedTokens", run.getMaxEstimatedTokens()));
        return new StartedRun(run, true);
    }

    /** Read the winning row in a fresh transaction after a concurrent unique-key insert lost. */
    @Transactional(readOnly = true)
    public java.util.Optional<PolicyAgentRun> replayExisting(User actor, String key,
            SourceChangeCandidate candidate, Scheme scheme, AgentExecutionMode mode) {
        return runs.findByStartedByIdAndIdempotencyKey(actor.getId(), key)
                .map(prior -> {
                    requireMatchingScope(prior, candidate, scheme, mode);
                    return prior;
                });
    }

    private void requireMatchingScope(PolicyAgentRun prior, SourceChangeCandidate candidate,
                                      Scheme scheme, AgentExecutionMode mode) {
        if (!prior.getSourceCandidate().getId().equals(candidate.getId())
                || !prior.getScheme().getId().equals(scheme.getId())
                || prior.getExecutionMode() != mode) {
            throw new PolicyInvestigationConflictException(
                    "This Idempotency-Key was already used for a different investigation scope or execution mode.");
        }
    }

    @Transactional
    public void recordTool(UUID runId, int toolCalls, int modelCalls, int estimatedTokens,
                           AgentEventType type, String toolName, long durationMs,
                           List<UUID> evidence, Map<String, Object> details) {
        PolicyAgentRun run = required(runId);
        requireRunning(run);
        run.setToolCallCount(toolCalls); run.setModelCallCount(modelCalls); run.setEstimatedTokensUsed(estimatedTokens);
        append(run, type, toolName, AgentEventOutcome.SUCCESS, durationMs, evidence, details);
    }

    @Transactional
    public void complete(UUID runId, PolicyInvestigationResult result) {
        PolicyAgentRun run = required(runId);
        requireRunning(run);
        run.setResult(result); run.setStatus(AgentRunStatus.COMPLETED); run.setCompletedAt(clock.instant());
        append(run, AgentEventType.RUN_COMPLETED, null, AgentEventOutcome.SUCCESS, null, result.snapshotIds(),
                Map.of("valid", result.validationIssues().isEmpty(), "publicationPerformed", false));
    }

    @Transactional
    public void fail(UUID runId, String message) {
        PolicyAgentRun run = required(runId);
        if (run.getStatus() != AgentRunStatus.RUNNING) return;
        run.setStatus(AgentRunStatus.FAILED); run.setFailureMessage(message); run.setCompletedAt(clock.instant());
        append(run, AgentEventType.RUN_FAILED, null, AgentEventOutcome.ERROR, null, List.of(), Map.of("message", message));
    }

    @Transactional
    public PolicyAgentRun cancel(UUID runId) {
        PolicyAgentRun run = required(runId);
        requireRunning(run);
        run.setStatus(AgentRunStatus.CANCELLED); run.setCancelledAt(clock.instant());
        append(run, AgentEventType.RUN_CANCELLED, null, AgentEventOutcome.SUCCESS, null, List.of(), Map.of());
        return run;
    }

    private PolicyAgentRun required(UUID runId) {
        return runs.findDetailedById(runId)
                .orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
    }

    private void requireRunning(PolicyAgentRun run) {
        if (run.getStatus() != AgentRunStatus.RUNNING) {
            throw new PolicyInvestigationException("Only a running investigation can change execution state.");
        }
    }

    private void append(PolicyAgentRun run, AgentEventType type, String toolName, AgentEventOutcome outcome,
                        Long durationMs, List<UUID> evidence, Map<String, Object> details) {
        PolicyAgentExecutionEvent event = new PolicyAgentExecutionEvent();
        event.setRun(run); event.setSequenceNumber((int) events.countByRunId(run.getId()) + 1);
        event.setEventType(type); event.setToolName(toolName); event.setOutcome(outcome); event.setDurationMs(durationMs);
        event.setEvidenceReferences(evidence); event.setDetails(details); event.setRecordedAt(clock.instant());
        events.save(event);
    }

    public record StartedRun(PolicyAgentRun run, boolean created) { }
}
