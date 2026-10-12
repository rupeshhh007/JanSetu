package com.jansetu.backend.source.change;

import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditService;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionCreateCommand;
import com.jansetu.backend.policy.registry.PolicyVersionService;
import com.jansetu.backend.source.SourceSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SourceCandidatePolicyDraftService {
    private final SourceChangeCandidateRepository candidates;
    private final PolicyVersionService policyVersions;
    private final AdminAuditService audit;
    private final Clock clock;

    @Autowired
    public SourceCandidatePolicyDraftService(SourceChangeCandidateRepository candidates,
                                             PolicyVersionService policyVersions, AdminAuditService audit) {
        this(candidates, policyVersions, audit, Clock.systemUTC());
    }

    SourceCandidatePolicyDraftService(SourceChangeCandidateRepository candidates,
                                      PolicyVersionService policyVersions, AdminAuditService audit, Clock clock) {
        this.candidates = candidates;
        this.policyVersions = policyVersions;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public PolicyVersion createDraft(UUID candidateId, LocalDate effectiveFrom, PolicyRule policyRule, User administrator) {
        SourceChangeCandidate candidate = candidates.lockById(candidateId)
                .orElseThrow(() -> new SourceChangeReviewException("Source-change candidate not found."));
        if (candidate.getStatus() != SourceChangeCandidateStatus.APPLICABLE || candidate.getAffectedScheme() == null) {
            throw new SourceChangeReviewException("Only accepted evidence associated with a scheme can create a policy draft.");
        }
        if (candidate.getProposedPolicyVersion() != null) {
            throw new SourceChangeReviewException("This source-change candidate already has a policy draft.");
        }
        if (administrator == null || administrator.getId() == null) {
            throw new SourceChangeReviewException("An authenticated administrator is required.");
        }
        SourceSnapshot evidence = candidate.getNewSnapshot();
        PolicySourceReference sourceReference = new PolicySourceReference(evidence.getId(),
                evidence.getSourceUrlAtCapture(), evidence.getSourceNameAtCapture(), evidence.getContentHash(),
                evidence.getContentExcerpt());
        PolicyVersion draft = policyVersions.createDraft(candidate.getAffectedScheme().getId(),
                new PolicyVersionCreateCommand(effectiveFrom, policyRule, List.of(sourceReference)));
        candidate.setProposedPolicyVersion(draft);
        candidate.setDraftedBy(administrator);
        candidate.setDraftedAt(Instant.now(clock));
        audit.record(administrator, AdminAuditAction.POLICY_DRAFT_CREATED, "POLICY_VERSION", draft.getId(),
                "Policy draft created from accepted source-change evidence.",
                Map.of("candidateId", candidate.getId().toString(),
                        "schemeId", candidate.getAffectedScheme().getId().toString(),
                        "snapshotId", evidence.getId().toString()));
        return draft;
    }
}
