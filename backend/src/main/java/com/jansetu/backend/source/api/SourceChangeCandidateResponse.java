package com.jansetu.backend.source.api;

import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.change.SourceChangeCandidate;
import com.jansetu.backend.source.change.SourceChangeCandidateStatus;

import java.time.Instant;
import java.util.UUID;

public record SourceChangeCandidateResponse(
        UUID id, SourceChangeCandidateStatus status, UUID sourceId, String sourceName, String sourceUrl,
        UUID previousSnapshotId, String previousHash, String previousExcerpt, Instant previousCapturedAt,
        UUID newSnapshotId, String newHash, String newExcerpt, Instant newCapturedAt,
        String changeSummary, UUID schemeId, String schemeName, String reviewNote,
        String reviewedBy, Instant reviewedAt, Instant createdAt
) {
    static SourceChangeCandidateResponse from(SourceChangeCandidate candidate) {
        SourceSnapshot previous = candidate.getPreviousSnapshot();
        SourceSnapshot current = candidate.getNewSnapshot();
        return new SourceChangeCandidateResponse(candidate.getId(), candidate.getStatus(), candidate.getSource().getId(),
                candidate.getSource().getName(), candidate.getSource().getCanonicalUrl(), previous.getId(), previous.getContentHash(), previous.getContentExcerpt(), previous.getCapturedAt(),
                current.getId(), current.getContentHash(), current.getContentExcerpt(), current.getCapturedAt(), candidate.getChangeSummary(),
                candidate.getAffectedScheme() == null ? null : candidate.getAffectedScheme().getId(),
                candidate.getAffectedScheme() == null ? null : candidate.getAffectedScheme().getName(), candidate.getReviewNote(),
                candidate.getReviewedBy() == null ? null : candidate.getReviewedBy().getEmail(), candidate.getReviewedAt(), candidate.getCreatedAt());
    }
}
