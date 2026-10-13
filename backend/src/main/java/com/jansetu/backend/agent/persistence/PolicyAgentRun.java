package com.jansetu.backend.agent.persistence;

import com.jansetu.backend.agent.domain.AgentExecutionMode;
import com.jansetu.backend.agent.domain.AgentRunStatus;
import com.jansetu.backend.agent.domain.PolicyInvestigationResult;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.source.change.SourceChangeCandidate;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_agent_runs", uniqueConstraints = @UniqueConstraint(
        name = "uq_policy_agent_run_submission", columnNames = {"started_by_user_id", "idempotency_key"}))
public class PolicyAgentRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "source_candidate_id", nullable = false)
    private SourceChangeCandidate sourceCandidate;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "started_by_user_id", nullable = false)
    private User startedBy;
    @Enumerated(EnumType.STRING) @Column(name = "execution_mode", nullable = false, length = 20)
    private AgentExecutionMode executionMode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private AgentRunStatus status;
    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;
    @Column(name = "max_tool_calls", nullable = false)
    private int maxToolCalls;
    @Column(name = "tool_call_count", nullable = false)
    private int toolCallCount;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private PolicyInvestigationResult result;
    @Column(name = "failure_message", length = 1000)
    private String failureMessage;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    @Version @Column(nullable = false)
    private long revision;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public SourceChangeCandidate getSourceCandidate() { return sourceCandidate; }
    public void setSourceCandidate(SourceChangeCandidate sourceCandidate) { this.sourceCandidate = sourceCandidate; }
    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }
    public User getStartedBy() { return startedBy; }
    public void setStartedBy(User startedBy) { this.startedBy = startedBy; }
    public AgentExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(AgentExecutionMode executionMode) { this.executionMode = executionMode; }
    public AgentRunStatus getStatus() { return status; }
    public void setStatus(AgentRunStatus status) { this.status = status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public int getMaxToolCalls() { return maxToolCalls; }
    public void setMaxToolCalls(int maxToolCalls) { this.maxToolCalls = maxToolCalls; }
    public int getToolCallCount() { return toolCallCount; }
    public void setToolCallCount(int toolCallCount) { this.toolCallCount = toolCallCount; }
    public PolicyInvestigationResult getResult() { return result; }
    public void setResult(PolicyInvestigationResult result) { this.result = result; }
    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
