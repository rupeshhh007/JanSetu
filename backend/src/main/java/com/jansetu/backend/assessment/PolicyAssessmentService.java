package com.jansetu.backend.assessment;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.policy.evaluation.PolicyEvaluation;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationContext;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationEngine;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionRepository;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.policy.CitizenProfilePolicyFactResolver;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import com.jansetu.backend.profile.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyAssessmentService {
    private final PolicyAssessmentRepository assessments;
    private final PolicyVersionRepository policyVersions;
    private final CitizenProfileRepository profiles;
    private final PolicyEvaluationEngine evaluator;
    private final Clock clock;
    private final CitizenProfilePolicyFactResolver profileFactResolver;

    @Autowired
    public PolicyAssessmentService(PolicyAssessmentRepository assessments, PolicyVersionRepository policyVersions,
                                   CitizenProfileRepository profiles, PolicyEvaluationEngine evaluator,
                                   CitizenProfilePolicyFactResolver profileFactResolver) {
        this(assessments, policyVersions, profiles, evaluator, Clock.systemUTC(), profileFactResolver);
    }

    PolicyAssessmentService(PolicyAssessmentRepository assessments, PolicyVersionRepository policyVersions,
                            CitizenProfileRepository profiles, PolicyEvaluationEngine evaluator, Clock clock) {
        this(assessments, policyVersions, profiles, evaluator, clock, null);
    }

    PolicyAssessmentService(PolicyAssessmentRepository assessments, PolicyVersionRepository policyVersions,
                            CitizenProfileRepository profiles, PolicyEvaluationEngine evaluator, Clock clock,
                            CitizenProfilePolicyFactResolver profileFactResolver) {
        this.assessments = assessments;
        this.policyVersions = policyVersions;
        this.profiles = profiles;
        this.evaluator = evaluator;
        this.clock = clock;
        this.profileFactResolver = profileFactResolver;
    }

    @Transactional
    public PolicyAssessment assessCurrent(User user, UUID schemeId) {
        CitizenProfile profile = profileFor(user);
        LocalDate today = LocalDate.now(clock);
        PolicyVersion version = policyVersions.findApplicableAt(schemeId, today)
                .orElseThrow(() -> new AssessmentNotFoundException("No reviewed policy version is available for this scheme."));
        return record(profile, version, today);
    }

    @Transactional
    public List<PolicyReassessment> reassessAll(PolicyVersion version) {
        LocalDate evaluationDate = LocalDate.now(clock);
        return assessments.findDistinctProfilesAssessedForScheme(version.getScheme().getId()).stream().map(profile -> {
            var previous = assessments.findTopByProfileIdAndPolicyVersionSchemeIdOrderByAssessedAtDesc(
                    profile.getId(), version.getScheme().getId());
            PolicyAssessment current = record(profile, version, evaluationDate);
            return new PolicyReassessment(profile, previous.map(PolicyAssessment::getPolicyResult).orElse(null), current);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<PolicyAssessment> history(User user) {
        return assessments.findByProfileIdOrderByAssessedAtDesc(profileFor(user).getId());
    }

    @Transactional(readOnly = true)
    public PolicyAssessmentReplay replay(User user, UUID assessmentId) {
        PolicyAssessment assessment = ownedAssessment(user, assessmentId);
        PolicyFacts facts = PolicyFacts.fromSnapshot(assessment.getFactSnapshot());
        PolicyEvaluation evaluation = evaluator.evaluate(assessment.getPolicyVersion().getPolicyRule(),
                new PolicyEvaluationContext(facts, assessment.getEvaluationDate()));
        return new PolicyAssessmentReplay(assessment.getId(), assessment.getPolicyResult(), evaluation.result(),
                assessment.getPolicyResult() == evaluation.result(), evaluation.trace());
    }

    private PolicyAssessment record(CitizenProfile profile, PolicyVersion version, LocalDate evaluationDate) {
        PolicyFacts facts = profileFactResolver == null
                ? com.jansetu.backend.profile.policy.CitizenProfilePolicyFacts.from(profile)
                : profileFactResolver.resolve(profile);
        PolicyEvaluation evaluation = evaluator.evaluate(version.getPolicyRule(),
                new PolicyEvaluationContext(facts, evaluationDate));
        PolicyAssessment assessment = new PolicyAssessment();
        assessment.setProfile(profile);
        assessment.setPolicyVersion(version);
        assessment.setEvaluationDate(evaluationDate);
        assessment.setPolicyResult(evaluation.result());
        assessment.setFactSnapshot(facts.snapshot());
        assessment.setEvaluationTrace(evaluation.trace());
        return assessments.save(assessment);
    }

    private CitizenProfile profileFor(User user) {
        return profiles.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Create your citizen profile before starting an assessment."));
    }

    private PolicyAssessment ownedAssessment(User user, UUID assessmentId) {
        PolicyAssessment assessment = assessments.findWithDetailsById(assessmentId)
                .orElseThrow(() -> new AssessmentNotFoundException("Assessment not found"));
        if (!assessment.getProfile().getId().equals(profileFor(user).getId())) {
            throw new AssessmentNotFoundException("Assessment not found");
        }
        return assessment;
    }
}
