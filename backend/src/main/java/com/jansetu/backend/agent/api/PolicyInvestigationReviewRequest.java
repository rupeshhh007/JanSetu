package com.jansetu.backend.agent.api;

import com.jansetu.backend.policy.domain.PolicyRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PolicyInvestigationReviewRequest(
        @NotNull AgentReviewDecision decision,
        @NotBlank @Size(max = 1000) String reason,
        PolicyRule proposedPolicyRule
) { }
