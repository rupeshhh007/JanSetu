package com.jansetu.backend.source.api;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.source.change.SourceChangeCandidateService;
import com.jansetu.backend.source.change.SourceCandidatePolicyDraftService;
import com.jansetu.backend.policy.api.PolicyVersionResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Admin-only source review queue. Acceptance links evidence to a JanSetu scheme; it never publishes policy. */
@RestController
@RequestMapping("/api/v1/admin/source-change-candidates")
public class SourceChangeCandidateController {
    private final SourceChangeCandidateService candidates;
    private final SourceCandidatePolicyDraftService policyDrafts;

    public SourceChangeCandidateController(SourceChangeCandidateService candidates,
                                           SourceCandidatePolicyDraftService policyDrafts) {
        this.candidates = candidates;
        this.policyDrafts = policyDrafts;
    }

    @GetMapping
    public List<SourceChangeCandidateResponse> pending() {
        return candidates.pendingReview().stream().map(SourceChangeCandidateResponse::from).toList();
    }

    @GetMapping("/history")
    public List<SourceChangeCandidateResponse> history() {
        return candidates.history().stream().map(SourceChangeCandidateResponse::from).toList();
    }

    @PostMapping("/{candidateId}/accept")
    public SourceChangeCandidateResponse accept(@PathVariable UUID candidateId, @Valid @RequestBody SourceChangeReviewRequest request,
                                                @AuthenticationPrincipal User user) {
        return SourceChangeCandidateResponse.from(candidates.accept(candidateId, request.schemeId(), request.reason(), user));
    }

    @PostMapping("/{candidateId}/reject")
    public SourceChangeCandidateResponse reject(@PathVariable UUID candidateId, @Valid @RequestBody SourceChangeReviewRequest request,
                                                @AuthenticationPrincipal User user) {
        return SourceChangeCandidateResponse.from(candidates.reject(candidateId, request.reason(), user));
    }

    @PostMapping("/{candidateId}/policy-draft")
    public PolicyVersionResponse createPolicyDraft(@PathVariable UUID candidateId,
                                                   @Valid @RequestBody SourceCandidatePolicyDraftRequest request,
                                                   @AuthenticationPrincipal User user) {
        return PolicyVersionResponse.from(policyDrafts.createDraft(candidateId, request.effectiveFrom(), request.policyRule(), user));
    }
}
