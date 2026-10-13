package com.jansetu.backend.preparation;

import com.jansetu.backend.auth.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "application_preparation_history")
public class ApplicationPreparationHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false)
    private ApplicationPreparationSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_user_id", nullable = false)
    private User actor;
    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false, length = 40)
    private PreparationHistoryEventType eventType;
    @Column(name = "task_key", length = 180) private String taskKey;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, String> details = new LinkedHashMap<>();
    @CreationTimestamp @Column(name = "recorded_at", nullable = false, updatable = false) private Instant recordedAt;

    public UUID getId() { return id; }
    public ApplicationPreparationSession getSession() { return session; }
    public void setSession(ApplicationPreparationSession session) { this.session = session; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public PreparationHistoryEventType getEventType() { return eventType; }
    public void setEventType(PreparationHistoryEventType eventType) { this.eventType = eventType; }
    public String getTaskKey() { return taskKey; }
    public void setTaskKey(String taskKey) { this.taskKey = taskKey; }
    public Map<String, String> getDetails() { return Map.copyOf(details); }
    public void setDetails(Map<String, String> details) { this.details = new LinkedHashMap<>(details); }
    public Instant getRecordedAt() { return recordedAt; }
}
