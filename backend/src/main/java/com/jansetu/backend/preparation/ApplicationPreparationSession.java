package com.jansetu.backend.preparation;

import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.scheme.entity.Scheme;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "application_preparation_sessions", uniqueConstraints = @UniqueConstraint(
        name = "uq_preparation_profile_scheme", columnNames = {"profile_id", "scheme_id"}))
public class ApplicationPreparationSession {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "profile_id", nullable = false)
    private CitizenProfile profile;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "policy_version_id") private PolicyVersion policyVersion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private PreparationStatus status;
    @Column(name = "scheme_revision", nullable = false) private long schemeRevision;
    @Column(name = "ready_at") private Instant readyAt;
    @Column(name = "citizen_reported_applied_at") private Instant citizenReportedAppliedAt;
    @Version @Column(nullable = false) private long revision;
    @CreationTimestamp @Column(nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(nullable = false) private Instant updatedAt;

    public UUID getId() { return id; }
    public CitizenProfile getProfile() { return profile; }
    public void setProfile(CitizenProfile profile) { this.profile = profile; }
    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }
    public PolicyVersion getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(PolicyVersion policyVersion) { this.policyVersion = policyVersion; }
    public PreparationStatus getStatus() { return status; }
    public void setStatus(PreparationStatus status) { this.status = status; }
    public long getSchemeRevision() { return schemeRevision; }
    public void setSchemeRevision(long schemeRevision) { this.schemeRevision = schemeRevision; }
    public Instant getReadyAt() { return readyAt; }
    public void setReadyAt(Instant readyAt) { this.readyAt = readyAt; }
    public Instant getCitizenReportedAppliedAt() { return citizenReportedAppliedAt; }
    public void setCitizenReportedAppliedAt(Instant value) { this.citizenReportedAppliedAt = value; }
    public long getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
