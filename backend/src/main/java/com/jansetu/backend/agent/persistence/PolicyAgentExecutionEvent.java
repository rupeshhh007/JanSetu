package com.jansetu.backend.agent.persistence;

import com.jansetu.backend.agent.domain.AgentEventOutcome;
import com.jansetu.backend.agent.domain.AgentEventType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "policy_agent_execution_events", uniqueConstraints = @UniqueConstraint(
        name = "uq_policy_agent_event_sequence", columnNames = {"run_id", "sequence_number"}))
public class PolicyAgentExecutionEvent {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "run_id", nullable = false)
    private PolicyAgentRun run;
    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 30)
    private AgentEventType eventType;
    @Column(name = "tool_name", length = 80)
    private String toolName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private AgentEventOutcome outcome;
    @Column(name = "duration_ms")
    private Long durationMs;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "evidence_references", columnDefinition = "jsonb", nullable = false)
    private List<UUID> evidenceReferences = new ArrayList<>();
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> details = new LinkedHashMap<>();
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    public UUID getId() { return id; }
    public PolicyAgentRun getRun() { return run; }
    public void setRun(PolicyAgentRun run) { this.run = run; }
    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public AgentEventType getEventType() { return eventType; }
    public void setEventType(AgentEventType eventType) { this.eventType = eventType; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public AgentEventOutcome getOutcome() { return outcome; }
    public void setOutcome(AgentEventOutcome outcome) { this.outcome = outcome; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public List<UUID> getEvidenceReferences() { return List.copyOf(evidenceReferences); }
    public void setEvidenceReferences(List<UUID> evidenceReferences) { this.evidenceReferences = new ArrayList<>(evidenceReferences); }
    public Map<String, Object> getDetails() { return Map.copyOf(details); }
    public void setDetails(Map<String, Object> details) { this.details = new LinkedHashMap<>(details); }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
