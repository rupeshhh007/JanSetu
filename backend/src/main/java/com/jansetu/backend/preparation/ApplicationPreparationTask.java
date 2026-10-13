package com.jansetu.backend.preparation;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "application_preparation_tasks", uniqueConstraints = @UniqueConstraint(
        name = "uq_preparation_task_key", columnNames = {"session_id", "task_key"}))
public class ApplicationPreparationTask {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false)
    private ApplicationPreparationSession session;
    @Column(name = "task_key", nullable = false, length = 180) private String taskKey;
    @Enumerated(EnumType.STRING) @Column(name = "task_type", nullable = false, length = 30) private PreparationTaskType taskType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private PreparationTaskStatus status;
    @Column(nullable = false, length = 500) private String title;
    @Column(length = 1000) private String guidance;
    @Column(name = "citizen_completable", nullable = false) private boolean citizenCompletable;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "evidence_references", columnDefinition = "jsonb", nullable = false)
    private List<PreparationEvidenceReference> evidenceReferences = new ArrayList<>();
    @Column(name = "completed_at") private Instant completedAt;
    @CreationTimestamp @Column(nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(nullable = false) private Instant updatedAt;

    public UUID getId() { return id; }
    public ApplicationPreparationSession getSession() { return session; }
    public void setSession(ApplicationPreparationSession session) { this.session = session; }
    public String getTaskKey() { return taskKey; }
    public void setTaskKey(String taskKey) { this.taskKey = taskKey; }
    public PreparationTaskType getTaskType() { return taskType; }
    public void setTaskType(PreparationTaskType taskType) { this.taskType = taskType; }
    public PreparationTaskStatus getStatus() { return status; }
    public void setStatus(PreparationTaskStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGuidance() { return guidance; }
    public void setGuidance(String guidance) { this.guidance = guidance; }
    public boolean isCitizenCompletable() { return citizenCompletable; }
    public void setCitizenCompletable(boolean value) { this.citizenCompletable = value; }
    public List<PreparationEvidenceReference> getEvidenceReferences() { return List.copyOf(evidenceReferences); }
    public void setEvidenceReferences(List<PreparationEvidenceReference> value) { evidenceReferences = new ArrayList<>(value); }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
