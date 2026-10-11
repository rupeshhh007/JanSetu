package com.jansetu.backend.policy.registry;

import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PolicyVersionServiceIntegrationTest {
    @Autowired
    private PolicyVersionService policyVersions;

    @Autowired
    private SchemeRepository schemes;

    private UUID schemeId;

    @BeforeEach
    void setUp() {
        Scheme scheme = new Scheme();
        scheme.setName("Policy-version fixture " + UUID.randomUUID());
        scheme.setDescription("A fixture used to verify immutable policy lifecycle transitions.");
        scheme.setState("Maharashtra");
        scheme.setDepartment("Test department");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setEligibilityCriteria(new EligibilityCriteria());
        scheme.setBenefits(List.of());
        scheme.setRequiredDocuments(List.of());
        schemeId = schemes.saveAndFlush(scheme).getId();
    }

    @Test
    void publishesAnApprovedVersionAndSupersedesThePreviousOneWithoutMutation() {
        PolicyVersion first = publish(LocalDate.of(2026, 10, 1), true);
        PolicyVersion second = publish(LocalDate.of(2026, 11, 1), false);

        List<PolicyVersion> history = policyVersions.findForScheme(schemeId);
        PolicyVersion superseded = history.stream().filter(version -> version.getId().equals(first.getId())).findFirst().orElseThrow();
        PolicyVersion active = history.stream().filter(version -> version.getId().equals(second.getId())).findFirst().orElseThrow();

        assertEquals(PolicyVersionStatus.SUPERSEDED, superseded.getStatus());
        assertEquals(LocalDate.of(2026, 10, 31), superseded.getEffectiveTo());
        assertEquals(PolicyVersionStatus.PUBLISHED, active.getStatus());
        assertEquals(2, active.getVersionNumber());
        assertEquals("student-rule", active.getPolicyRule().id());
    }

    @Test
    void rejectsPublicationBeforeApprovalAndRejectsUnverifiedSourceReferences() {
        PolicyVersion draft = policyVersions.createDraft(schemeId, command(LocalDate.of(2026, 10, 1), true));

        assertThrows(PolicyLifecycleException.class, () -> policyVersions.publish(draft.getId()));
        assertThrows(PolicyLifecycleException.class, () -> policyVersions.createDraft(schemeId,
                new PolicyVersionCreateCommand(LocalDate.of(2026, 10, 1),
                        new BooleanMatchRule("student-rule", "Recorded student requirement", FactKey.IS_STUDENT, true),
                        List.of())));
    }

    private PolicyVersion publish(LocalDate effectiveFrom, boolean expectedStudent) {
        PolicyVersion draft = policyVersions.createDraft(schemeId, command(effectiveFrom, expectedStudent));
        policyVersions.submitForReview(draft.getId());
        policyVersions.approve(draft.getId());
        return policyVersions.publish(draft.getId());
    }

    private PolicyVersionCreateCommand command(LocalDate effectiveFrom, boolean expectedStudent) {
        return new PolicyVersionCreateCommand(effectiveFrom,
                new BooleanMatchRule("student-rule", "Recorded student requirement", FactKey.IS_STUDENT, expectedStudent),
                List.of(new PolicySourceReference("https://mahadbt.maharashtra.gov.in/scheme/example",
                        "MahaDBT fixture", "sha256:fixture-content", "Recorded requirement excerpt.")));
    }
}
