package com.jansetu.backend.policy.registry;

import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.OfficialSourceRepository;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.SourceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
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
    @Autowired
    private OfficialSourceRepository sources;
    @Autowired
    private SourceSnapshotRepository snapshots;

    private UUID schemeId;
    private OfficialSource source;

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
        source = new OfficialSource();
        source.setName("MahaDBT fixture");
        source.setAuthority("Government of Maharashtra");
        source.setCanonicalUrl("https://mahadbt.maharashtra.gov.in/scheme/example");
        source.setNormalizedHost("mahadbt.maharashtra.gov.in");
        source = sources.saveAndFlush(source);
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

        assertThrows(PolicyLifecycleException.class, () -> policyVersions.publish(schemeId, draft.getId()));
        assertThrows(PolicyLifecycleException.class, () -> policyVersions.createDraft(schemeId,
                new PolicyVersionCreateCommand(LocalDate.of(2026, 10, 1),
                        new BooleanMatchRule("student-rule", "Recorded student requirement", FactKey.IS_STUDENT, true),
                        List.of())));
        assertThrows(PolicyLifecycleException.class, () -> policyVersions.createDraft(schemeId,
                new PolicyVersionCreateCommand(LocalDate.of(2026, 10, 1),
                        new BooleanMatchRule("student-rule", "Recorded student requirement", FactKey.IS_STUDENT, true),
                        List.of(new PolicySourceReference(UUID.randomUUID(), null, null, null, null)))));
    }

    @Test
    void doesNotMutateAPolicyWhenTheSchemePathDoesNotOwnIt() {
        PolicyVersion draft = policyVersions.createDraft(schemeId, command(LocalDate.of(2026, 10, 1), true));

        assertThrows(PolicyVersionNotFoundException.class,
                () -> policyVersions.submitForReview(UUID.randomUUID(), draft.getId()));

        assertEquals(PolicyVersionStatus.DRAFT, policyVersions.findForScheme(schemeId, draft.getId()).getStatus());
    }

    private PolicyVersion publish(LocalDate effectiveFrom, boolean expectedStudent) {
        PolicyVersion draft = policyVersions.createDraft(schemeId, command(effectiveFrom, expectedStudent));
        policyVersions.submitForReview(schemeId, draft.getId());
        policyVersions.approve(schemeId, draft.getId());
        return policyVersions.publish(schemeId, draft.getId());
    }

    private PolicyVersionCreateCommand command(LocalDate effectiveFrom, boolean expectedStudent) {
        return new PolicyVersionCreateCommand(effectiveFrom,
                new BooleanMatchRule("student-rule", "Recorded student requirement", FactKey.IS_STUDENT, expectedStudent),
                List.of(sourceReference("fixture-content-" + effectiveFrom)));
    }

    private PolicySourceReference sourceReference(String marker) {
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSource(source);
        snapshot.setSourceNameAtCapture(source.getName());
        snapshot.setSourceUrlAtCapture(source.getCanonicalUrl());
        snapshot.setContentType("text/plain");
        snapshot.setHttpStatus(200);
        snapshot.setContentLength(marker.length());
        snapshot.setContentHash("sha256:" + marker);
        snapshot.setContentExcerpt("Recorded requirement excerpt.");
        snapshot.setContentBytes(marker.getBytes());
        snapshot.setCapturedAt(Instant.parse("2026-10-01T00:00:00Z"));
        snapshot = snapshots.saveAndFlush(snapshot);
        return new PolicySourceReference(snapshot.getId(), null, null, null, null);
    }
}
