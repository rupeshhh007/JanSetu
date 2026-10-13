package com.jansetu.backend.agent.service;

import com.jansetu.backend.agent.api.AgentReviewDecision;
import com.jansetu.backend.agent.api.PolicyInvestigationReviewRequest;
import com.jansetu.backend.agent.domain.*;
import com.jansetu.backend.agent.persistence.PolicyAgentRun;
import com.jansetu.backend.agent.persistence.PolicyAgentRunRepository;
import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditService;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.PolicyRule;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.source.change.SourceCandidatePolicyDraftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PolicyInvestigationReviewService {
    private final PolicyAgentRunRepository runs;
    private final PolicyProposalValidator validator;
    private final SourceCandidatePolicyDraftService drafts;
    private final AdminAuditService audit;

    public PolicyInvestigationReviewService(PolicyAgentRunRepository runs, PolicyProposalValidator validator,
                                            SourceCandidatePolicyDraftService drafts, AdminAuditService audit) {
        this.runs = runs; this.validator = validator; this.drafts = drafts; this.audit = audit;
    }

    @Transactional
    public PolicyAgentRun review(UUID runId, PolicyInvestigationReviewRequest request, User administrator) {
        authorize(administrator);
        PolicyAgentRun run = lockCompleted(runId);
        if (run.getReviewStatus() != AgentReviewStatus.PENDING_REVIEW) {
            throw new PolicyInvestigationConflictException("This investigation already has a human review decision.");
        }
        if (request == null || request.decision() == null || request.reason() == null
                || request.reason().isBlank() || request.reason().length() > 1000) {
            throw new PolicyInvestigationException("A review decision and reason of at most 1000 characters are required.");
        }
        String reason = request.reason().trim();
        if (request.decision() == AgentReviewDecision.REJECT) {
            run.setReviewStatus(AgentReviewStatus.REJECTED);
            applyReview(run, administrator, reason);
            audit.record(administrator, AdminAuditAction.AGENT_INVESTIGATION_REJECTED, "POLICY_AGENT_RUN", run.getId(), reason,
                    Map.of("candidateId", run.getSourceCandidate().getId().toString(), "schemeId", run.getScheme().getId().toString()));
            return run;
        }
        if (run.getSourceCandidate().getAffectedScheme() != null
                && !run.getSourceCandidate().getAffectedScheme().getId().equals(run.getScheme().getId())) {
            throw new PolicyInvestigationConflictException(
                    "The evidence was accepted for a different scheme after this investigation started.");
        }
        PolicyInvestigationResult original = requireResult(run);
        PolicyRule editedRule = request.proposedPolicyRule() == null ? original.proposedPolicyRule() : request.proposedPolicyRule();
        PolicyInvestigationResult edited = new PolicyInvestigationResult(original.investigationId(), original.status(),
                original.sourceCandidateId(), original.schemeId(), original.snapshotIds(), original.previousPolicyVersionId(),
                original.proposedChangeSummary(), editedRule, original.evidenceCitations(), original.unverifiedClaims(),
                java.util.List.of(), true, original.recommendedNextAction(), original.deterministicFixture());
        var issues = validator.validate(edited, run.getSourceCandidate().getId(), run.getScheme().getId(), Set.of(
                run.getSourceCandidate().getPreviousSnapshot().getId(), run.getSourceCandidate().getNewSnapshot().getId()));
        if (!issues.isEmpty()) {
            throw new PolicyInvestigationException("The edited proposal failed deterministic validation: " + issues.getFirst().message());
        }
        run.setResult(new PolicyInvestigationResult(edited.investigationId(), edited.status(), edited.sourceCandidateId(),
                edited.schemeId(), edited.snapshotIds(), edited.previousPolicyVersionId(), edited.proposedChangeSummary(),
                edited.proposedPolicyRule(), edited.evidenceCitations(), edited.unverifiedClaims(), issues, true,
                edited.recommendedNextAction(), edited.deterministicFixture()));
        run.setReviewStatus(AgentReviewStatus.APPROVED);
        applyReview(run, administrator, reason);
        audit.record(administrator, AdminAuditAction.AGENT_INVESTIGATION_APPROVED, "POLICY_AGENT_RUN", run.getId(), reason,
                Map.of("candidateId", run.getSourceCandidate().getId().toString(), "schemeId", run.getScheme().getId().toString()));
        return run;
    }

    @Transactional
    public PolicyVersion forward(UUID runId, LocalDate effectiveFrom, User administrator) {
        authorize(administrator);
        runs.lockById(runId)
                .orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
        PolicyAgentRun run = runs.findForReviewById(runId)
                .orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
        if (run.getReviewStatus() != AgentReviewStatus.APPROVED) {
            throw new PolicyInvestigationConflictException("Only an administrator-approved investigation can create a policy draft.");
        }
        if (run.getSourceCandidate().getAffectedScheme() == null
                || !run.getSourceCandidate().getAffectedScheme().getId().equals(run.getScheme().getId())) {
            throw new PolicyInvestigationConflictException(
                    "The reviewed evidence is not accepted for this investigation's scheme.");
        }
        PolicyInvestigationResult result = requireResult(run);
        PolicyVersion draft = drafts.createDraft(run.getSourceCandidate().getId(), effectiveFrom,
                result.proposedPolicyRule(), administrator);
        run.setReviewStatus(AgentReviewStatus.FORWARDED_TO_POLICY_DRAFT);
        audit.record(administrator, AdminAuditAction.AGENT_INVESTIGATION_FORWARDED, "POLICY_AGENT_RUN", run.getId(),
                run.getReviewReason(), Map.of("policyVersionId", draft.getId().toString(),
                        "candidateId", run.getSourceCandidate().getId().toString()));
        return draft;
    }

    private PolicyAgentRun lockCompleted(UUID id) {
        runs.lockById(id)
                .orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
        PolicyAgentRun run = runs.findForReviewById(id)
                .orElseThrow(() -> new PolicyInvestigationException("The investigation does not exist."));
        if (run.getStatus() != AgentRunStatus.COMPLETED) {
            throw new PolicyInvestigationException("Only a completed investigation can be reviewed.");
        }
        return run;
    }

    private PolicyInvestigationResult requireResult(PolicyAgentRun run) {
        if (run.getResult() == null) throw new PolicyInvestigationException("The investigation has no structured proposal.");
        return run.getResult();
    }

    private void applyReview(PolicyAgentRun run, User actor, String reason) {
        run.setReviewedBy(actor); run.setReviewReason(reason); run.setReviewedAt(Instant.now());
    }

    private void authorize(User actor) {
        if (actor == null || actor.getId() == null || actor.getRole() != Role.ADMIN || !actor.isEnabled()) {
            throw new PolicyInvestigationException("Administrator authorization is required.");
        }
    }
}
