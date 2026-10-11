package com.jansetu.backend.policy.api;

import com.jansetu.backend.policy.registry.PolicyVersionCreateCommand;
import com.jansetu.backend.policy.registry.PolicyVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Administrative policy-registry API. SecurityConfig restricts this namespace to administrators. */
@RestController
@RequestMapping("/api/v1/admin/schemes/{schemeId}/policy-versions")
public class PolicyVersionController {
    private final PolicyVersionService policyVersions;

    public PolicyVersionController(PolicyVersionService policyVersions) {
        this.policyVersions = policyVersions;
    }

    @PostMapping
    public ResponseEntity<PolicyVersionResponse> createDraft(@PathVariable UUID schemeId,
                                                               @Valid @RequestBody PolicyVersionRequest request) {
        var created = policyVersions.createDraft(schemeId,
                new PolicyVersionCreateCommand(request.effectiveFrom(), request.policyRule(), request.sourceReferences()));
        return ResponseEntity.status(HttpStatus.CREATED).body(PolicyVersionResponse.from(created));
    }

    @GetMapping
    public List<PolicyVersionResponse> history(@PathVariable UUID schemeId) {
        return policyVersions.findForScheme(schemeId).stream().map(PolicyVersionResponse::from).toList();
    }

    @PostMapping("/{policyVersionId}/submit-review")
    public PolicyVersionResponse submitForReview(@PathVariable UUID schemeId, @PathVariable UUID policyVersionId) {
        return responseForScheme(schemeId, policyVersions.submitForReview(policyVersionId));
    }

    @PostMapping("/{policyVersionId}/approve")
    public PolicyVersionResponse approve(@PathVariable UUID schemeId, @PathVariable UUID policyVersionId) {
        return responseForScheme(schemeId, policyVersions.approve(policyVersionId));
    }

    @PostMapping("/{policyVersionId}/publish")
    public PolicyVersionResponse publish(@PathVariable UUID schemeId, @PathVariable UUID policyVersionId) {
        return responseForScheme(schemeId, policyVersions.publish(policyVersionId));
    }

    private PolicyVersionResponse responseForScheme(UUID schemeId, com.jansetu.backend.policy.registry.PolicyVersion version) {
        if (!version.getScheme().getId().equals(schemeId)) {
            throw new com.jansetu.backend.policy.registry.PolicyVersionNotFoundException("Policy version not found for scheme");
        }
        return PolicyVersionResponse.from(version);
    }
}
