package com.jansetu.backend.scheme.service;

import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.exception.ProfileNotFoundException;
import com.jansetu.backend.profile.service.CitizenProfileService;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluation;
import com.jansetu.backend.scheme.evaluator.EligibilityEvaluator;
import com.jansetu.backend.scheme.exception.SchemeNotFoundException;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class SchemeEligibilityService {
    private final SchemeRepository schemes;
    private final CitizenProfileService profiles;
    private final CurrentUserService users;
    private final EligibilityEvaluator evaluator;

    public SchemeEligibilityService(SchemeRepository schemes, CitizenProfileService profiles,
                                    CurrentUserService users, EligibilityEvaluator evaluator) {
        this.schemes = schemes;
        this.profiles = profiles;
        this.users = users;
        this.evaluator = evaluator;
    }

    public EligibilityEvaluation check(UUID id) {
        Scheme scheme = schemes.findById(id)
                .filter(s -> s.getStatus() == SchemeStatus.PUBLISHED)
                .orElseThrow(() -> new SchemeNotFoundException("Published scheme not found"));
        CitizenProfile profile;
        try {
            profile = profiles.getProfileEntity(users.getCurrentUser());
        } catch (ProfileNotFoundException ex) {
            profile = null;
        }
        return evaluator.evaluateScheme(profile, scheme);
    }
}
