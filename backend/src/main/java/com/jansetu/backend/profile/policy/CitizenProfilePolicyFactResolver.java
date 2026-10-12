package com.jansetu.backend.profile.policy;

import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.fact.CitizenProfileFactService;
import org.springframework.stereotype.Component;

/** Resolves the active profile-fact ledger at the policy boundary. */
@Component
public class CitizenProfilePolicyFactResolver {
    private final CitizenProfileFactService profileFacts;

    public CitizenProfilePolicyFactResolver(CitizenProfileFactService profileFacts) {
        this.profileFacts = profileFacts;
    }

    public PolicyFacts resolve(CitizenProfile profile) {
        return CitizenProfilePolicyFacts.from(profile, profileFacts.activeFacts(profile));
    }
}
