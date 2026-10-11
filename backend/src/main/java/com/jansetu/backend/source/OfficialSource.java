package com.jansetu.backend.source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** A manually approved official authority from which policy evidence may be captured. */
@Entity
@Table(name = "official_sources")
public class OfficialSource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 160)
    private String authority;

    @Column(name = "canonical_url", nullable = false, length = 2048, unique = true)
    private String canonicalUrl;

    @Column(name = "normalized_host", nullable = false, length = 255)
    private String normalizedHost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfficialSourceStatus status = OfficialSourceStatus.ACTIVE;

    @Column(name = "last_snapshot_at")
    private Instant lastSnapshotAt;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getCanonicalUrl() { return canonicalUrl; }
    public void setCanonicalUrl(String canonicalUrl) { this.canonicalUrl = canonicalUrl; }
    public String getNormalizedHost() { return normalizedHost; }
    public void setNormalizedHost(String normalizedHost) { this.normalizedHost = normalizedHost; }
    public OfficialSourceStatus getStatus() { return status; }
    public void setStatus(OfficialSourceStatus status) { this.status = status; }
    public Instant getLastSnapshotAt() { return lastSnapshotAt; }
    public void setLastSnapshotAt(Instant lastSnapshotAt) { this.lastSnapshotAt = lastSnapshotAt; }
    public long getRevision() { return revision; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
