package com.jansetu.backend.profile.fact;

import com.jansetu.backend.policy.domain.FactKey;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** One current value per closed policy fact key, with assurance and reuse controls. */
@Entity
@Table(name = "citizen_profile_facts", uniqueConstraints = @UniqueConstraint(
        name = "uq_citizen_profile_facts_profile_key",
        columnNames = {"profile_id", "fact_key"}
))
public class CitizenProfileFact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private CitizenProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "fact_key", nullable = false, length = 64)
    private FactKey factKey;

    /** Canonical representation defined by the closed FactKey type. */
    @Column(name = "value_text", nullable = false)
    private String valueText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProfileFactProvenance provenance;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 32)
    private ProfileFactVerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_scope", nullable = false, length = 32)
    private ProfileFactConsentScope consentScope = ProfileFactConsentScope.PRIVATE;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

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
    public CitizenProfile getProfile() { return profile; }
    public void setProfile(CitizenProfile profile) { this.profile = profile; }
    public FactKey getFactKey() { return factKey; }
    public void setFactKey(FactKey factKey) { this.factKey = factKey; }
    public String getValueText() { return valueText; }
    public void setValueText(String valueText) { this.valueText = valueText; }
    public ProfileFactProvenance getProvenance() { return provenance; }
    public void setProvenance(ProfileFactProvenance provenance) { this.provenance = provenance; }
    public ProfileFactVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(ProfileFactVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public ProfileFactConsentScope getConsentScope() { return consentScope; }
    public void setConsentScope(ProfileFactConsentScope consentScope) { this.consentScope = consentScope; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
    public long getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
