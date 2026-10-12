package com.jansetu.backend.scheme.evaluator;

import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyFacts;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.evaluation.PolicyEvaluation;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationContext;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationEngine;
import com.jansetu.backend.policy.evaluation.PolicyEvaluationTrace;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.policy.CitizenProfilePolicyFacts;
import com.jansetu.backend.profile.policy.CitizenProfilePolicyFactResolver;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.policy.LegacyEligibilityPolicyAdapter;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Compatibility boundary for the original catalogue. It delegates all
 * criterion evaluation to the typed policy engine while retaining citizen-safe
 * explanatory copy for the current API contract.
 */
@Component
public class EligibilityEvaluator {
    private final PolicyEvaluationEngine policyEngine;
    private final LegacyEligibilityPolicyAdapter legacyPolicyAdapter;
    private final Clock clock;
    private final CitizenProfilePolicyFactResolver profileFactResolver;

    @Autowired
    public EligibilityEvaluator(PolicyEvaluationEngine policyEngine,
                                LegacyEligibilityPolicyAdapter legacyPolicyAdapter,
                                CitizenProfilePolicyFactResolver profileFactResolver) {
        this(policyEngine, legacyPolicyAdapter, Clock.systemUTC(), profileFactResolver);
    }

    EligibilityEvaluator(PolicyEvaluationEngine policyEngine,
                         LegacyEligibilityPolicyAdapter legacyPolicyAdapter,
                         Clock clock) {
        this(policyEngine, legacyPolicyAdapter, clock, null);
    }

    EligibilityEvaluator(PolicyEvaluationEngine policyEngine,
                         LegacyEligibilityPolicyAdapter legacyPolicyAdapter,
                         Clock clock,
                         CitizenProfilePolicyFactResolver profileFactResolver) {
        this.policyEngine = policyEngine;
        this.legacyPolicyAdapter = legacyPolicyAdapter;
        this.clock = clock;
        this.profileFactResolver = profileFactResolver;
    }

    /** Retained for existing plain unit tests. */
    public EligibilityEvaluator() {
        this(new PolicyEvaluationEngine(), new LegacyEligibilityPolicyAdapter(), Clock.systemUTC(), null);
    }

    /** Shared publication and jurisdiction checks for discovery and preparation. */
    public EligibilityEvaluation evaluateScheme(CitizenProfile profile, Scheme scheme) {
        if (scheme.getStatus() != SchemeStatus.PUBLISHED) {
            return new EligibilityEvaluation(EligibilityOutcome.NOT_ELIGIBLE, List.of(),
                    List.of("This scheme is not currently published."), List.of(), 0);
        }
        if (profile == null) {
            return new EligibilityEvaluation(EligibilityOutcome.NEEDS_INFORMATION, List.of(), List.of(),
                    List.of("Create your citizen profile to check the recorded requirements."), 0);
        }
        String state = scheme.getState();
        if (state != null && !state.isBlank() && !state.equalsIgnoreCase("India") && !state.equalsIgnoreCase("Central")
                && (profile.getState() == null || !state.equalsIgnoreCase(profile.getState().trim()))) {
            return new EligibilityEvaluation(EligibilityOutcome.NOT_ELIGIBLE, List.of(),
                    List.of("This scheme is available to residents of " + state + "."), List.of(), 1);
        }
        return evaluate(profile, scheme.getEligibilityCriteria());
    }

    public boolean isEligible(CitizenProfile profile, EligibilityCriteria criteria) {
        return evaluate(profile, criteria).isEligible();
    }

    /** Uses today's UTC date only for the legacy live-catalogue flow. */
    public EligibilityEvaluation evaluate(CitizenProfile profile, EligibilityCriteria criteria) {
        return evaluate(profile, criteria, LocalDate.now(clock));
    }

    /** Uses an explicit date so a recorded assessment can be replayed later. */
    public EligibilityEvaluation evaluate(CitizenProfile profile, EligibilityCriteria criteria, LocalDate evaluationDate) {
        var policy = legacyPolicyAdapter.toPolicy(criteria);
        if (policy == null) {
            return new EligibilityEvaluation(EligibilityOutcome.NEEDS_INFORMATION, List.of(), List.of(),
                    List.of("The scheme's eligibility rules have not been recorded yet."), 0);
        }

        PolicyFacts facts = profileFactResolver == null
                ? CitizenProfilePolicyFacts.from(profile)
                : profileFactResolver.resolve(profile);
        PolicyEvaluation evaluation = policyEngine.evaluate(policy,
                new PolicyEvaluationContext(facts, evaluationDate));
        List<PolicyEvaluationTrace> leaves = leafTraces(evaluation.trace());
        List<String> matched = leaves.stream().filter(trace -> trace.result() == PolicyResult.SATISFIED)
                .map(this::messageForSatisfied).toList();
        List<String> unmet = leaves.stream().filter(trace -> trace.result() == PolicyResult.FAILED)
                .map(this::messageForFailed).toList();
        List<String> missing = evaluation.missingFacts().stream().map(this::messageForMissing).toList();
        EligibilityOutcome outcome = switch (evaluation.result()) {
            case SATISFIED -> EligibilityOutcome.ELIGIBLE;
            case FAILED -> EligibilityOutcome.NOT_ELIGIBLE;
            case UNKNOWN, NOT_APPLICABLE -> EligibilityOutcome.NEEDS_INFORMATION;
        };
        return new EligibilityEvaluation(outcome, matched, unmet, missing, leaves.size(), evaluation.trace());
    }

    private List<PolicyEvaluationTrace> leafTraces(PolicyEvaluationTrace trace) {
        if (trace.children().isEmpty()) {
            return List.of(trace);
        }
        return trace.children().stream().flatMap(child -> leafTraces(child).stream()).toList();
    }

    private String messageForSatisfied(PolicyEvaluationTrace trace) {
        return switch (trace.ruleId()) {
            case "legacy-age" -> "Your age meets the recorded requirement.";
            case "legacy-income" -> "Your recorded family income is within the scheme's limit.";
            case "legacy-category" -> "Your recorded social category is included in the scheme rules.";
            case "legacy-occupation" -> "Your recorded occupation is included in the scheme rules.";
            case "legacy-student" -> "You are recorded as a student.";
            case "legacy-farmer" -> "You are recorded as a farmer.";
            case "legacy-disability" -> "You are recorded as a person with a disability.";
            default -> trace.label() + " is satisfied.";
        };
    }

    private String messageForFailed(PolicyEvaluationTrace trace) {
        return switch (trace.ruleId()) {
            case "legacy-age" -> "Your recorded age does not meet this scheme's requirement.";
            case "legacy-income" -> "Annual family income is above this scheme's recorded limit.";
            case "legacy-category" -> "This scheme is recorded for specific social categories.";
            case "legacy-occupation" -> "Your recorded occupation is not among this scheme's listed occupations.";
            case "legacy-student" -> "This scheme requires the applicant to be a student.";
            case "legacy-farmer" -> "This scheme requires the applicant to be a farmer.";
            case "legacy-disability" -> "This scheme requires the applicant to have a disability.";
            default -> trace.label() + " is not satisfied.";
        };
    }

    private String messageForMissing(FactKey fact) {
        return switch (fact) {
            case BIRTH_DATE -> "Date of birth is needed to check the age requirement.";
            case ANNUAL_FAMILY_INCOME -> "Annual family income is needed to check the income limit.";
            case CATEGORY -> "Social category is needed to check this scheme requirement.";
            case PRIMARY_OCCUPATION -> "Primary occupation is needed to check this scheme requirement.";
            default -> fact.displayName() + " is needed to check the recorded requirements.";
        };
    }
}
