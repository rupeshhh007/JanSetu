package com.jansetu.backend.assessment;

import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.profile.entity.CitizenProfile;

public record PolicyReassessment(
        CitizenProfile profile,
        PolicyResult previousResult,
        PolicyAssessment assessment
) { }
