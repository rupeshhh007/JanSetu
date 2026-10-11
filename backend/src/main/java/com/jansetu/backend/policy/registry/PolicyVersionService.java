package com.jansetu.backend.policy.registry;

import com.jansetu.backend.policy.validation.PolicyRuleValidator;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Manages the controlled lifecycle for policy content, never mutating a published definition. */
@Service
public class PolicyVersionService {
    private final PolicyVersionRepository policyVersions;
    private final SchemeRepository schemes;
    private final PolicyRuleValidator validator;
    private final Clock clock;

    @Autowired
    public PolicyVersionService(PolicyVersionRepository policyVersions, SchemeRepository schemes,
                                PolicyRuleValidator validator) {
        this(policyVersions, schemes, validator, Clock.systemUTC());
    }

    PolicyVersionService(PolicyVersionRepository policyVersions, SchemeRepository schemes,
                         PolicyRuleValidator validator, Clock clock) {
        this.policyVersions = policyVersions;
        this.schemes = schemes;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public PolicyVersion createDraft(UUID schemeId, PolicyVersionCreateCommand command) {
        validator.validateOrThrow(command.policyRule());
        validateSourceReferences(command.sourceReferences());
        if (command.effectiveFrom() == null) {
            throw new PolicyLifecycleException("An effective date is required for a policy version.");
        }
        Scheme scheme = schemes.findById(schemeId)
                .orElseThrow(() -> new SchemeNotFoundException("Scheme not found"));
        int nextVersion = policyVersions.findTopBySchemeIdOrderByVersionNumberDesc(schemeId)
                .map(version -> version.getVersionNumber() + 1)
                .orElse(1);
        PolicyVersion version = new PolicyVersion();
        version.setScheme(scheme);
        version.setVersionNumber(nextVersion);
        version.setEffectiveFrom(command.effectiveFrom());
        version.setRecordedAt(Instant.now(clock));
        version.setPolicyRule(command.policyRule());
        version.setSourceReferences(command.sourceReferences());
        return policyVersions.save(version);
    }

    @Transactional
    public PolicyVersion submitForReview(UUID id) {
        PolicyVersion version = find(id);
        transition(version, PolicyVersionStatus.DRAFT, PolicyVersionStatus.IN_REVIEW);
        return version;
    }

    @Transactional
    public PolicyVersion approve(UUID id) {
        PolicyVersion version = find(id);
        transition(version, PolicyVersionStatus.IN_REVIEW, PolicyVersionStatus.APPROVED);
        version.setApprovedAt(Instant.now(clock));
        return version;
    }

    @Transactional
    public PolicyVersion publish(UUID id) {
        PolicyVersion candidate = find(id);
        if (candidate.getStatus() != PolicyVersionStatus.APPROVED) {
            throw new PolicyLifecycleException("Only an approved policy version may be published.");
        }
        validator.validateOrThrow(candidate.getPolicyRule());
        PolicyVersion current = policyVersions.lockCurrentPublishedVersion(candidate.getScheme().getId()).orElse(null);
        if (current != null) {
            if (!candidate.getEffectiveFrom().isAfter(current.getEffectiveFrom())) {
                throw new PolicyLifecycleException("A replacement policy must begin after the active policy version.");
            }
            current.setEffectiveTo(candidate.getEffectiveFrom().minusDays(1));
            current.setSupersededAt(Instant.now(clock));
            current.setStatus(PolicyVersionStatus.SUPERSEDED);
        }
        candidate.setStatus(PolicyVersionStatus.PUBLISHED);
        candidate.setPublishedAt(Instant.now(clock));
        return candidate;
    }

    @Transactional(readOnly = true)
    public List<PolicyVersion> findForScheme(UUID schemeId) {
        return policyVersions.findBySchemeIdOrderByVersionNumberDesc(schemeId);
    }

    private PolicyVersion find(UUID id) {
        return policyVersions.findById(id)
                .orElseThrow(() -> new PolicyVersionNotFoundException("Policy version not found"));
    }

    private void transition(PolicyVersion version, PolicyVersionStatus expected, PolicyVersionStatus next) {
        if (version.getStatus() != expected) {
            throw new PolicyLifecycleException("Policy version must be " + expected + " before it can move to " + next + ".");
        }
        version.setStatus(next);
    }

    private void validateSourceReferences(List<PolicySourceReference> sourceReferences) {
        if (sourceReferences.isEmpty()) {
            throw new PolicyLifecycleException("At least one official source reference is required for a policy version.");
        }
        for (PolicySourceReference source : sourceReferences) {
            if (source == null || source.sourceUrl() == null || source.sourceUrl().isBlank()
                    || source.sourceTitle() == null || source.sourceTitle().isBlank()
                    || source.contentHash() == null || source.contentHash().isBlank()) {
                throw new PolicyLifecycleException("Every source reference needs a URL, title, and content hash.");
            }
            URI uri;
            try {
                uri = URI.create(source.sourceUrl());
            } catch (IllegalArgumentException exception) {
                throw new PolicyLifecycleException("Policy source URLs must be valid HTTP or HTTPS URLs.");
            }
            if (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme())) {
                throw new PolicyLifecycleException("Policy source URLs must use HTTP or HTTPS.");
            }
        }
    }
}
