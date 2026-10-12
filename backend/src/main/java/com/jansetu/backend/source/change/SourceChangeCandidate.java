package com.jansetu.backend.source.change;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.policy.registry.PolicyVersion;
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
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Immutable evidence links plus a reviewable interpretation of a meaningful source change. */
@Entity
@Table(name = "source_change_candidates")
public class SourceChangeCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private OfficialSource source;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "previous_snapshot_id", nullable = false)
    private SourceSnapshot previousSnapshot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "new_snapshot_id", nullable = false, unique = true)
    private SourceSnapshot newSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceChangeCandidateStatus status = SourceChangeCandidateStatus.PENDING_REVIEW;

    @Column(name = "change_summary", columnDefinition = "TEXT", nullable = false)
    private String changeSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_scheme_id")
    private Scheme affectedScheme;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_policy_version_id", unique = true)
    private PolicyVersion proposedPolicyVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drafted_by_user_id")
    private User draftedBy;

    @Column(name = "drafted_at")
    private Instant draftedAt;

    @Version
    @Column(name = "revision", nullable = false)
    private long revision;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public OfficialSource getSource() { return source; }
    public void setSource(OfficialSource source) { this.source = source; }
    public SourceSnapshot getPreviousSnapshot() { return previousSnapshot; }
    public void setPreviousSnapshot(SourceSnapshot previousSnapshot) { this.previousSnapshot = previousSnapshot; }
    public SourceSnapshot getNewSnapshot() { return newSnapshot; }
    public void setNewSnapshot(SourceSnapshot newSnapshot) { this.newSnapshot = newSnapshot; }
    public SourceChangeCandidateStatus getStatus() { return status; }
    public void setStatus(SourceChangeCandidateStatus status) { this.status = status; }
    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }
    public Scheme getAffectedScheme() { return affectedScheme; }
    public void setAffectedScheme(Scheme affectedScheme) { this.affectedScheme = affectedScheme; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public PolicyVersion getProposedPolicyVersion() { return proposedPolicyVersion; }
    public void setProposedPolicyVersion(PolicyVersion proposedPolicyVersion) { this.proposedPolicyVersion = proposedPolicyVersion; }
    public User getDraftedBy() { return draftedBy; }
    public void setDraftedBy(User draftedBy) { this.draftedBy = draftedBy; }
    public Instant getDraftedAt() { return draftedAt; }
    public void setDraftedAt(Instant draftedAt) { this.draftedAt = draftedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
