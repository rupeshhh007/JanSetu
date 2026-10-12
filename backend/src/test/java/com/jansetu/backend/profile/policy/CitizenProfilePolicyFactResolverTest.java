package com.jansetu.backend.profile.policy;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.fact.CitizenProfileFact;
import com.jansetu.backend.profile.fact.CitizenProfileFactService;
import com.jansetu.backend.profile.fact.ProfileFactConsentScope;
import com.jansetu.backend.profile.fact.ProfileFactProvenance;
import com.jansetu.backend.profile.fact.ProfileFactVerificationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitizenProfilePolicyFactResolverTest {
    @Test
    void activeLedgerFactOverridesTheLegacyFieldAtThePolicyBoundary() {
        CitizenProfile profile = new CitizenProfile();
        profile.setAnnualFamilyIncome(new BigDecimal("500000"));
        CitizenProfileFact verifiedIncome = new CitizenProfileFact();
        verifiedIncome.setFactKey(FactKey.ANNUAL_FAMILY_INCOME);
        verifiedIncome.setValueText("250000");
        verifiedIncome.setProvenance(ProfileFactProvenance.DOCUMENT_VERIFIED);
        verifiedIncome.setVerificationStatus(ProfileFactVerificationStatus.DOCUMENT_VERIFIED);
        verifiedIncome.setConsentScope(ProfileFactConsentScope.PRIVATE);
        CitizenProfileFactService facts = mock(CitizenProfileFactService.class);
        when(facts.activeFacts(profile)).thenReturn(List.of(verifiedIncome));

        var resolved = new CitizenProfilePolicyFactResolver(facts).resolve(profile);

        assertEquals(new BigDecimal("250000"), resolved.decimal(FactKey.ANNUAL_FAMILY_INCOME).orElseThrow());
    }
}
