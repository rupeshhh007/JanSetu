package com.jansetu.backend.policy.registry;

import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.scheme.entity.Scheme;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A durable, immutable-after-publication policy definition for one scheme. */
@Entity
@Table(name = "policy_versions", uniqueConstraints = @UniqueConstraint(
        name = "uq_policy_versions_scheme_version", columnNames = {"scheme_id", "version_number"}))
public class PolicyVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyVersionStatus status = PolicyVersionStatus.DRAFT;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "superseded_at")
    private Instant supersededAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy_rule", columnDefinition = "jsonb", nullable = false)
    private PolicyRule policyRule;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_references", columnDefinition = "jsonb", nullable = false)
    private List<PolicySourceReference> sourceReferences = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private long revision;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public PolicyVersionStatus getStatus() { return status; }
    public void setStatus(PolicyVersionStatus status) { this.status = status; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getSupersededAt() { return supersededAt; }
    public void setSupersededAt(Instant supersededAt) { this.supersededAt = supersededAt; }
    public PolicyRule getPolicyRule() { return policyRule; }
    public void setPolicyRule(PolicyRule policyRule) { this.policyRule = policyRule; }
    public List<PolicySourceReference> getSourceReferences() { return List.copyOf(sourceReferences); }
    public void setSourceReferences(List<PolicySourceReference> sourceReferences) {
        this.sourceReferences = sourceReferences == null ? new ArrayList<>() : new ArrayList<>(sourceReferences);
    }
    public long getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
