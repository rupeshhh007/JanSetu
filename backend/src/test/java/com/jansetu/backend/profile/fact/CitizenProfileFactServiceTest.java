package com.jansetu.backend.profile.fact;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitizenProfileFactServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-12T00:00:00Z");

    @Test
    void selfDeclaredUpdateCannotOverwriteANewerVerifiedFact() {
        CitizenProfileFactRepository facts = mock(CitizenProfileFactRepository.class);
        CitizenProfileRepository profiles = mock(CitizenProfileRepository.class);
        CitizenProfileFactService service = service(facts, profiles);
        CitizenProfile profile = profile();
        User user = new User();
        CitizenProfileFact verified = fact(profile, FactKey.ANNUAL_FAMILY_INCOME, "250000",
                ProfileFactProvenance.DOCUMENT_VERIFIED, ProfileFactVerificationStatus.DOCUMENT_VERIFIED,
                ProfileFactConsentScope.PRIVATE, null);
        when(profiles.findByUser(user)).thenReturn(Optional.of(profile));
        when(facts.findByProfileIdAndFactKey(profile.getId(), FactKey.ANNUAL_FAMILY_INCOME)).thenReturn(Optional.of(verified));

        CitizenProfileFact result = service.submitCitizenFact(
                user, FactKey.ANNUAL_FAMILY_INCOME, "300000", true, null);

        assertEquals(verified, result);
        assertEquals("250000", result.getValueText());
        assertEquals(ProfileFactConsentScope.CASEWORKER_REUSE, result.getConsentScope());
    }

    @Test
    void caseworkerReadPathRequiresConsentAndFiltersExpiredFacts() {
        CitizenProfileFactRepository facts = mock(CitizenProfileFactRepository.class);
        CitizenProfileFactService service = service(facts, mock(CitizenProfileRepository.class));
        CitizenProfile profile = profile();
        CitizenProfileFact privateFact = fact(profile, FactKey.STATE, "Maharashtra",
                ProfileFactProvenance.CITIZEN_DECLARED, ProfileFactVerificationStatus.SELF_DECLARED,
                ProfileFactConsentScope.PRIVATE, null);
        CitizenProfileFact expiredFact = fact(profile, FactKey.IS_STUDENT, "true",
                ProfileFactProvenance.CITIZEN_DECLARED, ProfileFactVerificationStatus.SELF_DECLARED,
                ProfileFactConsentScope.CASEWORKER_REUSE, NOW.minusSeconds(1));
        CitizenProfileFact reusableFact = fact(profile, FactKey.IS_FARMER, "false",
                ProfileFactProvenance.CITIZEN_DECLARED, ProfileFactVerificationStatus.SELF_DECLARED,
                ProfileFactConsentScope.CASEWORKER_REUSE, NOW.plusSeconds(1));
        when(facts.findByProfileIdOrderByFactKeyAsc(profile.getId()))
                .thenReturn(List.of(privateFact, expiredFact, reusableFact));

        List<CitizenProfileFact> reusable = service.reusableFactsForCaseworker(profile);

        assertEquals(List.of(reusableFact), reusable);
        assertFalse(service.isActive(expiredFact));
        assertTrue(service.isActive(reusableFact));
    }

    @Test
    void citizenCanGrantReuseConsentWithoutReplacingAVerifiedValue() {
        CitizenProfileFactRepository facts = mock(CitizenProfileFactRepository.class);
        CitizenProfileRepository profiles = mock(CitizenProfileRepository.class);
        CitizenProfileFactService service = service(facts, profiles);
        CitizenProfile profile = profile();
        User user = new User();
        CitizenProfileFact verified = fact(profile, FactKey.STATE, "Maharashtra",
                ProfileFactProvenance.OFFICIAL_RECORD, ProfileFactVerificationStatus.AUTHORITATIVE,
                ProfileFactConsentScope.PRIVATE, null);
        when(profiles.findByUser(user)).thenReturn(Optional.of(profile));
        when(facts.findByProfileIdAndFactKey(profile.getId(), FactKey.STATE)).thenReturn(Optional.of(verified));
        when(facts.save(verified)).thenReturn(verified);

        CitizenProfileFact result = service.submitCitizenFact(user, FactKey.STATE, "Goa", true, null);

        assertEquals("Maharashtra", result.getValueText());
        assertEquals(ProfileFactConsentScope.CASEWORKER_REUSE, result.getConsentScope());
        verify(facts).save(verified);
    }

    @Test
    void profileSynchronizationPreservesTheExistingReuseConsent() {
        CitizenProfileFactRepository facts = mock(CitizenProfileFactRepository.class);
        CitizenProfileFactService service = service(facts, mock(CitizenProfileRepository.class));
        CitizenProfile profile = profile();
        CitizenProfileFact existing = fact(profile, FactKey.STATE, "Maharashtra",
                ProfileFactProvenance.CITIZEN_DECLARED, ProfileFactVerificationStatus.SELF_DECLARED,
                ProfileFactConsentScope.CASEWORKER_REUSE, null);
        when(facts.findByProfileIdAndFactKey(profile.getId(), FactKey.STATE)).thenReturn(Optional.of(existing));

        CitizenProfileFact result = service.synchronizeSelfDeclaredProfileFact(profile, FactKey.STATE, "Goa");

        assertEquals("Goa", result.getValueText());
        assertEquals(ProfileFactConsentScope.CASEWORKER_REUSE, result.getConsentScope());
    }

    @Test
    void rejectsInvalidAndAlreadyExpiredCitizenFacts() {
        CitizenProfileFactRepository facts = mock(CitizenProfileFactRepository.class);
        CitizenProfileRepository profiles = mock(CitizenProfileRepository.class);
        CitizenProfileFactService service = service(facts, profiles);
        CitizenProfile profile = profile();
        User user = new User();
        when(profiles.findByUser(user)).thenReturn(Optional.of(profile));

        assertThrows(InvalidProfileFactException.class,
                () -> service.submitCitizenFact(user, FactKey.IS_STUDENT, "sometimes", false, null));
        assertThrows(InvalidProfileFactException.class,
                () -> service.submitCitizenFact(user, FactKey.STATE, "Maharashtra", false, NOW));
    }

    private CitizenProfileFactService service(
            CitizenProfileFactRepository facts,
            CitizenProfileRepository profiles
    ) {
        return new CitizenProfileFactService(facts, profiles, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private CitizenProfile profile() {
        CitizenProfile profile = mock(CitizenProfile.class);
        when(profile.getId()).thenReturn(UUID.randomUUID());
        return profile;
    }

    private CitizenProfileFact fact(
            CitizenProfile profile,
            FactKey factKey,
            String value,
            ProfileFactProvenance provenance,
            ProfileFactVerificationStatus verificationStatus,
            ProfileFactConsentScope consentScope,
            Instant expiresAt
    ) {
        CitizenProfileFact fact = new CitizenProfileFact();
        fact.setProfile(profile);
        fact.setFactKey(factKey);
        fact.setValueText(value);
        fact.setProvenance(provenance);
        fact.setVerificationStatus(verificationStatus);
        fact.setConsentScope(consentScope);
        fact.setExpiresAt(expiresAt);
        fact.setCapturedAt(NOW);
        return fact;
    }
}
