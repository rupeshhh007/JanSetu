package com.jansetu.backend.assessment;

import com.jansetu.backend.policy.domain.PolicyFactSnapshot;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationTrace;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.profile.entity.CitizenProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Immutable record of an assessment made against one exact policy version. */
@Entity
@Table(name = "policy_assessments")
public class PolicyAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private CitizenProfile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_version_id", nullable = false)
    private PolicyVersion policyVersion;

    @Column(name = "evaluation_date", nullable = false)
    private LocalDate evaluationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_result", nullable = false, length = 20)
    private PolicyResult policyResult;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fact_snapshot", columnDefinition = "jsonb", nullable = false)
    private List<PolicyFactSnapshot> factSnapshot = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evaluation_trace", columnDefinition = "jsonb", nullable = false)
    private PolicyEvaluationTrace evaluationTrace;

    @CreationTimestamp
    @Column(name = "assessed_at", nullable = false, updatable = false)
    private Instant assessedAt;

    public UUID getId() { return id; }
    public CitizenProfile getProfile() { return profile; }
    public void setProfile(CitizenProfile profile) { this.profile = profile; }
    public PolicyVersion getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(PolicyVersion policyVersion) { this.policyVersion = policyVersion; }
    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }
    public PolicyResult getPolicyResult() { return policyResult; }
    public void setPolicyResult(PolicyResult policyResult) { this.policyResult = policyResult; }
    public List<PolicyFactSnapshot> getFactSnapshot() { return List.copyOf(factSnapshot); }
    public void setFactSnapshot(List<PolicyFactSnapshot> factSnapshot) {
        this.factSnapshot = factSnapshot == null ? new ArrayList<>() : new ArrayList<>(factSnapshot);
    }
    public PolicyEvaluationTrace getEvaluationTrace() { return evaluationTrace; }
    public void setEvaluationTrace(PolicyEvaluationTrace evaluationTrace) { this.evaluationTrace = evaluationTrace; }
    public Instant getAssessedAt() { return assessedAt; }
}
