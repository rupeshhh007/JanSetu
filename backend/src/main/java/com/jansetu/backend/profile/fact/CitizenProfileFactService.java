package com.jansetu.backend.profile.fact;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.exception.ProfileNotFoundException;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Writes citizen-supplied facts without letting a lower-assurance value replace
 * a verified one. Caseworker-facing code must use reusableFactsForCaseworker,
 * which applies consent and expiry filtering in one place.
 */
@Service
public class CitizenProfileFactService {
    private final CitizenProfileFactRepository facts;
    private final CitizenProfileRepository profiles;
    private final Clock clock;

    @Autowired
    public CitizenProfileFactService(CitizenProfileFactRepository facts, CitizenProfileRepository profiles) {
        this(facts, profiles, Clock.systemUTC());
    }

    CitizenProfileFactService(CitizenProfileFactRepository facts, CitizenProfileRepository profiles, Clock clock) {
        this.facts = facts;
        this.profiles = profiles;
        this.clock = clock;
    }

    @Transactional
    public CitizenProfileFact submitCitizenFact(
            User user,
            FactKey factKey,
            String value,
            boolean consentToCaseworkerReuse,
            Instant expiresAt
    ) {
        CitizenProfile profile = profiles.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Create your citizen profile before adding information."));
        return recordCitizenDeclaredFact(profile, factKey, value, consentToCaseworkerReuse, expiresAt);
    }

    /** Used by the legacy profile form to maintain a private, self-declared fact ledger. */
    @Transactional
    public CitizenProfileFact recordCitizenDeclaredFact(
            CitizenProfile profile,
            FactKey factKey,
            String value,
            boolean consentToCaseworkerReuse,
            Instant expiresAt
    ) {
        return recordFact(profile, factKey, value,
                ProfileFactProvenance.CITIZEN_DECLARED,
                ProfileFactVerificationStatus.SELF_DECLARED,
                consentToCaseworkerReuse ? ProfileFactConsentScope.CASEWORKER_REUSE : ProfileFactConsentScope.PRIVATE,
                expiresAt);
    }

    /**
     * Mirrors legacy profile fields without changing an existing reuse choice.
     * A profile save updates the self-declared value, but consent remains an
     * explicit action in the fact ledger.
     */
    @Transactional
    public CitizenProfileFact synchronizeSelfDeclaredProfileFact(
            CitizenProfile profile,
            FactKey factKey,
            String value
    ) {
        if (profile == null || profile.getId() == null || factKey == null) {
            throw new InvalidProfileFactException("A profile and fact key are required.");
        }
        ProfileFactConsentScope existingConsent = facts.findByProfileIdAndFactKey(profile.getId(), factKey)
                .map(CitizenProfileFact::getConsentScope)
                .orElse(ProfileFactConsentScope.PRIVATE);
        return recordFact(profile, factKey, value,
                ProfileFactProvenance.CITIZEN_DECLARED,
                ProfileFactVerificationStatus.SELF_DECLARED,
                existingConsent,
                null);
    }

    /** Internal trust boundary for future document and official-record ingestion. */
    @Transactional
    public CitizenProfileFact recordTrustedFact(
            CitizenProfile profile,
            FactKey factKey,
            String value,
            ProfileFactProvenance provenance,
            ProfileFactVerificationStatus verificationStatus,
            ProfileFactConsentScope consentScope,
            Instant expiresAt
    ) {
        if (provenance == ProfileFactProvenance.CITIZEN_DECLARED
                || verificationStatus == ProfileFactVerificationStatus.SELF_DECLARED) {
            throw new InvalidProfileFactException("Trusted facts must record a verified provenance and verification status.");
        }
        return recordFact(profile, factKey, value, provenance, verificationStatus, consentScope, expiresAt);
    }

    @Transactional(readOnly = true)
    public List<CitizenProfileFact> factsForCitizen(User user) {
        CitizenProfile profile = profiles.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
        return facts.findByProfileIdOrderByFactKeyAsc(profile.getId());
    }

    @Transactional(readOnly = true)
    public List<CitizenProfileFact> activeFacts(CitizenProfile profile) {
        return facts.findByProfileIdOrderByFactKeyAsc(profile.getId()).stream()
                .filter(this::isActive)
                .toList();
    }

    /** The only supported read path for future caseworker flows. */
    @Transactional(readOnly = true)
    public List<CitizenProfileFact> reusableFactsForCaseworker(CitizenProfile profile) {
        return activeFacts(profile).stream()
                .filter(fact -> fact.getConsentScope() == ProfileFactConsentScope.CASEWORKER_REUSE)
                .toList();
    }

    public boolean isActive(CitizenProfileFact fact) {
        return fact.getExpiresAt() == null || fact.getExpiresAt().isAfter(Instant.now(clock));
    }

    private CitizenProfileFact recordFact(
            CitizenProfile profile,
            FactKey factKey,
            String value,
            ProfileFactProvenance provenance,
            ProfileFactVerificationStatus verificationStatus,
            ProfileFactConsentScope consentScope,
            Instant expiresAt
    ) {
        if (profile == null || profile.getId() == null || factKey == null
                || provenance == null || verificationStatus == null || consentScope == null) {
            throw new InvalidProfileFactException("A profile, fact key, provenance, verification status, and consent scope are required.");
        }
        Instant now = Instant.now(clock);
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new InvalidProfileFactException("A fact expiry must be in the future.");
        }
        String canonicalValue = canonicalize(factKey, value);
        CitizenProfileFact existing = facts.findByProfileIdAndFactKey(profile.getId(), factKey).orElse(null);
        if (existing != null && isRicher(existing, provenance, verificationStatus)) {
            // Consent belongs to the citizen even when a trusted source owns the value.
            existing.setConsentScope(consentScope);
            facts.save(existing);
            return existing;
        }
        CitizenProfileFact target = existing == null ? new CitizenProfileFact() : existing;
        target.setProfile(profile);
        target.setFactKey(factKey);
        target.setValueText(canonicalValue);
        target.setProvenance(provenance);
        target.setVerificationStatus(verificationStatus);
        target.setConsentScope(consentScope);
        target.setExpiresAt(expiresAt);
        target.setCapturedAt(now);
        facts.save(target);
        return target;
    }

    private boolean isRicher(
            CitizenProfileFact existing,
            ProfileFactProvenance incomingProvenance,
            ProfileFactVerificationStatus incomingVerification
    ) {
        int verificationComparison = Integer.compare(
                existing.getVerificationStatus().assuranceRank(), incomingVerification.assuranceRank());
        if (verificationComparison != 0) {
            return verificationComparison > 0;
        }
        return existing.getProvenance().assuranceRank() > incomingProvenance.assuranceRank();
    }

    private String canonicalize(FactKey factKey, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new InvalidProfileFactException(factKey.displayName() + " is required.");
        }
        try {
            Object typedValue = switch (factKey.valueType()) {
                case DECIMAL -> new BigDecimal(rawValue.trim());
                case DATE -> LocalDate.parse(rawValue.trim());
                case BOOLEAN -> parseBoolean(rawValue);
                case TEXT -> rawValue.trim();
            };
            return PolicyFacts.builder().put(factKey, typedValue).build().snapshot().getFirst().value();
        } catch (RuntimeException exception) {
            if (exception instanceof InvalidProfileFactException profileFactException) {
                throw profileFactException;
            }
            throw new InvalidProfileFactException("Provide a valid " + factKey.valueType().name().toLowerCase() + " for "
                    + factKey.displayName() + ".");
        }
    }

    private boolean parseBoolean(String rawValue) {
        if (!"true".equalsIgnoreCase(rawValue) && !"false".equalsIgnoreCase(rawValue)) {
            throw new InvalidProfileFactException("Provide true or false for the selected fact.");
        }
        return Boolean.parseBoolean(rawValue);
    }
}
