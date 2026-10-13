package com.jansetu.backend.agent.service;

import com.jansetu.backend.agent.domain.EvidenceCitation;
import com.jansetu.backend.agent.domain.PolicyInvestigationResult;
import com.jansetu.backend.policy.validation.PolicyRuleValidator;
import com.jansetu.backend.policy.validation.PolicyValidationIssue;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.SourceSnapshotRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class PolicyProposalValidator {
    private static final int MAX_CITATION_LENGTH = 2_000;

    private final PolicyRuleValidator rules;
    private final SourceSnapshotRepository snapshots;

    public PolicyProposalValidator(PolicyRuleValidator rules, SourceSnapshotRepository snapshots) {
        this.rules = rules;
        this.snapshots = snapshots;
    }

    public List<PolicyValidationIssue> validate(PolicyInvestigationResult proposal,
                                                UUID expectedCandidateId,
                                                UUID expectedSchemeId,
                                                Set<UUID> allowedSnapshotIds) {
        List<PolicyValidationIssue> issues = new ArrayList<>(rules.validate(proposal.proposedPolicyRule()));
        if (!expectedCandidateId.equals(proposal.sourceCandidateId())) {
            issues.add(issue("CANDIDATE_SCOPE_MISMATCH", "The proposal references a different source candidate."));
        }
        if (!expectedSchemeId.equals(proposal.schemeId())) {
            issues.add(issue("SCHEME_SCOPE_MISMATCH", "The proposal references a different scheme."));
        }
        if (proposal.evidenceCitations().isEmpty()) {
            issues.add(issue("EVIDENCE_REQUIRED", "At least one exact source citation is required."));
        }
        if (proposal.evidenceCitations().size() > 10) {
            issues.add(issue("TOO_MANY_CITATIONS", "A policy investigation may cite at most ten bounded evidence spans."));
        }
        for (EvidenceCitation citation : proposal.evidenceCitations()) {
            validateCitation(citation, allowedSnapshotIds, issues);
        }
        return List.copyOf(issues);
    }

    private void validateCitation(EvidenceCitation citation, Set<UUID> allowedSnapshotIds,
                                  List<PolicyValidationIssue> issues) {
        if (citation == null || citation.snapshotId() == null || !allowedSnapshotIds.contains(citation.snapshotId())) {
            issues.add(issue("UNSCOPED_EVIDENCE", "Every citation must reference a snapshot from this candidate."));
            return;
        }
        String span = citation.exactSpan() == null ? "" : citation.exactSpan().trim();
        if (span.isEmpty() || span.length() > MAX_CITATION_LENGTH) {
            issues.add(issue("INVALID_EVIDENCE_SPAN", "Citation spans must contain 1 to 2000 characters."));
            return;
        }
        SourceSnapshot snapshot = snapshots.findById(citation.snapshotId()).orElse(null);
        if (snapshot == null) {
            issues.add(issue("EVIDENCE_NOT_FOUND", "A cited snapshot no longer exists."));
            return;
        }
        String fullContent = new String(snapshot.getContentBytes(), StandardCharsets.UTF_8);
        if (!snapshot.getContentExcerpt().contains(span) && !fullContent.contains(span)) {
            issues.add(issue("EVIDENCE_SPAN_MISMATCH", "A citation is not an exact span of its immutable snapshot."));
        }
        if (!snapshot.getSourceNameAtCapture().equals(citation.sourceTitle())) {
            issues.add(issue("EVIDENCE_TITLE_MISMATCH", "The cited source name must match the immutable capture."));
        }
        if (!snapshot.getSourceUrlAtCapture().equals(citation.sourceUrl())) {
            issues.add(issue("EVIDENCE_URL_MISMATCH", "The cited source URL must match the captured immutable snapshot."));
        }
        if (!snapshot.getContentHash().equals(citation.contentHash())) {
            issues.add(issue("EVIDENCE_HASH_MISMATCH", "A citation content hash does not match its immutable snapshot."));
        }
    }

    private PolicyValidationIssue issue(String code, String message) {
        return new PolicyValidationIssue(null, code, message);
    }
}
