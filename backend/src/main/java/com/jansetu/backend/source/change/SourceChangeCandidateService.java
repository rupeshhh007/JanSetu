package com.jansetu.backend.source.change;

import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditService;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import com.jansetu.backend.source.SourceSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Creates a review queue entry only for a meaningful, deterministic text change. */
@Service
public class SourceChangeCandidateService {
    private final SourceChangeCandidateRepository candidates;
    private final SourceContentNormalizer normalizer;
    private final SchemeRepository schemes;
    private final AdminAuditService audit;
    private final Clock clock;

    @Autowired
    public SourceChangeCandidateService(SourceChangeCandidateRepository candidates, SourceContentNormalizer normalizer,
                                        SchemeRepository schemes, AdminAuditService audit) {
        this(candidates, normalizer, schemes, audit, Clock.systemUTC());
    }

    SourceChangeCandidateService(SourceChangeCandidateRepository candidates, SourceContentNormalizer normalizer,
                                 SchemeRepository schemes, AdminAuditService audit, Clock clock) {
        this.candidates = candidates;
        this.normalizer = normalizer;
        this.schemes = schemes;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public Optional<SourceChangeCandidate> detect(SourceSnapshot previous, SourceSnapshot current) {
        if (previous == null || previous.getContentHash().equals(current.getContentHash())) return Optional.empty();
        Optional<String> previousText = normalizer.normalizedText(previous);
        Optional<String> currentText = normalizer.normalizedText(current);
        if (previousText.isEmpty() || currentText.isEmpty() || previousText.get().equals(currentText.get())) {
            return Optional.empty();
        }
        SourceChangeCandidate candidate = new SourceChangeCandidate();
        candidate.setSource(current.getSource());
        candidate.setPreviousSnapshot(previous);
        candidate.setNewSnapshot(current);
        candidate.setChangeSummary(summary(previous, current, previousText.get(), currentText.get()));
        return Optional.of(candidates.save(candidate));
    }

    @Transactional(readOnly = true)
    public List<SourceChangeCandidate> pendingReview() {
        return candidates.findByStatusOrderByCreatedAtDesc(SourceChangeCandidateStatus.PENDING_REVIEW);
    }

    @Transactional
    public SourceChangeCandidate accept(UUID candidateId, UUID schemeId, String reason, User reviewer) {
        SourceChangeCandidate candidate = lockedPending(candidateId);
        if (schemeId == null) throw new SourceChangeReviewException("Select the JanSetu scheme affected by this evidence.");
        Scheme scheme = schemes.findById(schemeId).orElseThrow(() -> new SchemeNotFoundException("Scheme not found"));
        candidate.setAffectedScheme(scheme);
        candidate.setStatus(SourceChangeCandidateStatus.APPLICABLE);
        applyReview(candidate, reviewer, reason);
        audit.record(reviewer, AdminAuditAction.SOURCE_CHANGE_ACCEPTED, "SOURCE_CHANGE_CANDIDATE", candidate.getId(), reason,
                Map.of("sourceId", candidate.getSource().getId().toString(), "schemeId", scheme.getId().toString(),
                        "newSnapshotId", candidate.getNewSnapshot().getId().toString()));
        return candidate;
    }

    @Transactional
    public SourceChangeCandidate reject(UUID candidateId, String reason, User reviewer) {
        SourceChangeCandidate candidate = lockedPending(candidateId);
        candidate.setStatus(SourceChangeCandidateStatus.REJECTED);
        applyReview(candidate, reviewer, reason);
        audit.record(reviewer, AdminAuditAction.SOURCE_CHANGE_REJECTED, "SOURCE_CHANGE_CANDIDATE", candidate.getId(), reason,
                Map.of("sourceId", candidate.getSource().getId().toString(), "newSnapshotId", candidate.getNewSnapshot().getId().toString()));
        return candidate;
    }

    private SourceChangeCandidate lockedPending(UUID candidateId) {
        SourceChangeCandidate candidate = candidates.lockById(candidateId)
                .orElseThrow(() -> new SourceChangeReviewException("Source-change candidate not found."));
        if (candidate.getStatus() != SourceChangeCandidateStatus.PENDING_REVIEW) {
            throw new SourceChangeReviewException("This source-change candidate has already been reviewed.");
        }
        return candidate;
    }

    private void applyReview(SourceChangeCandidate candidate, User reviewer, String reason) {
        if (reviewer == null || reviewer.getId() == null) throw new SourceChangeReviewException("An authenticated administrator is required.");
        if (reason == null || reason.isBlank()) throw new SourceChangeReviewException("Record a reason for this review decision.");
        candidate.setReviewNote(reason.trim());
        candidate.setReviewedBy(reviewer);
        candidate.setReviewedAt(Instant.now(clock));
    }

    private String summary(SourceSnapshot previous, SourceSnapshot current, String previousText, String currentText) {
        return "Meaningful text changed: " + previousText.length() + " to " + currentText.length()
                + " normalized characters (" + previous.getContentHash() + " → " + current.getContentHash() + ")."
                + " Administrative review is required before this evidence can affect a JanSetu policy.";
    }
}
