package com.jansetu.backend.assessment;

import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionCreateCommand;
import com.jansetu.backend.policy.registry.PolicyVersionService;
import com.jansetu.backend.profile.entity.Category;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.profile.entity.Gender;
import com.jansetu.backend.profile.entity.MaritalStatus;
import com.jansetu.backend.profile.entity.Occupation;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.OfficialSourceRepository;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.SourceSnapshotRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PolicyAssessmentServiceIntegrationTest {
    @Autowired private PolicyAssessmentService assessments;
    @Autowired private PolicyAssessmentRepository assessmentRepository;
    @Autowired private PolicyVersionService policyVersions;
    @Autowired private SchemeRepository schemes;
    @Autowired private UserRepository users;
    @Autowired private CitizenProfileRepository profiles;
    @Autowired private OfficialSourceRepository sources;
    @Autowired private SourceSnapshotRepository snapshots;
    @Autowired private EntityManager entityManager;

    private User citizen;
    private User otherCitizen;
    private UUID schemeId;
    private OfficialSource source;

    @BeforeEach
    void setUp() {
        citizen = createCitizen("citizen-" + UUID.randomUUID() + "@jansetu.test", true);
        otherCitizen = createCitizen("other-" + UUID.randomUUID() + "@jansetu.test", false);
        Scheme scheme = new Scheme();
        scheme.setName("Assessment fixture " + UUID.randomUUID());
        scheme.setDescription("A fixture for immutable assessment-ledger verification.");
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
        publish(LocalDate.of(2026, 1, 1), true);
    }

    @Test
    void persistsATypeStableSnapshotAndReplaysAfterASuccessorIsPublished() {
        PolicyAssessment recorded = assessments.assessCurrent(citizen, schemeId);
        assessmentRepository.flush();
        entityManager.clear();
        publish(LocalDate.of(2026, 11, 1), false);

        PolicyAssessmentReplay replay = assessments.replay(citizen, recorded.getId());

        assertEquals(PolicyResult.SATISFIED, recorded.getPolicyResult());
        assertEquals(PolicyResult.SATISFIED, replay.replayedResult());
        assertTrue(replay.matchesRecordedResult());
        assertEquals("true", assessmentRepository.findById(recorded.getId()).orElseThrow().getFactSnapshot().stream()
                .filter(fact -> fact.fact() == FactKey.IS_STUDENT).findFirst().orElseThrow().value());
    }

    @Test
    void doesNotExposeAnotherCitizensAssessment() {
        PolicyAssessment recorded = assessments.assessCurrent(citizen, schemeId);

        AssessmentNotFoundException exception = assertThrows(AssessmentNotFoundException.class,
                () -> assessments.replay(otherCitizen, recorded.getId()));

        assertEquals("Assessment not found", exception.getMessage());
    }

    private void publish(LocalDate effectiveFrom, boolean expectedStudent) {
        PolicyVersion draft = policyVersions.createDraft(schemeId,
                new PolicyVersionCreateCommand(effectiveFrom,
                        new BooleanMatchRule("student-status", "Recorded student requirement", FactKey.IS_STUDENT,
                                expectedStudent),
                        List.of(sourceReference(effectiveFrom))));
        policyVersions.submitForReview(schemeId, draft.getId());
        policyVersions.approve(schemeId, draft.getId());
        policyVersions.publish(schemeId, draft.getId());
    }

    private PolicySourceReference sourceReference(LocalDate effectiveFrom) {
        String marker = "fixture-" + effectiveFrom;
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
        snapshot.setCapturedAt(Instant.parse("2026-01-01T00:00:00Z"));
        snapshot = snapshots.saveAndFlush(snapshot);
        return new PolicySourceReference(snapshot.getId(), null, null, null, null);
    }

    private User createCitizen(String email, boolean student) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("not-used-by-this-test");
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);
        user = users.saveAndFlush(user);

        CitizenProfile profile = new CitizenProfile();
        profile.setUser(user);
        profile.setFirstName("Synthetic");
        profile.setLastName("Citizen");
        profile.setDateOfBirth(LocalDate.of(2002, 1, 1));
        profile.setGender(Gender.OTHER);
        profile.setState("Maharashtra");
        profile.setDistrict("Pune");
        profile.setPincode("411001");
        profile.setCategory(Category.GENERAL);
        profile.setAnnualFamilyIncome(new BigDecimal("100000"));
        profile.setMaritalStatus(MaritalStatus.SINGLE);
        profile.setPrimaryOccupation(student ? Occupation.STUDENT : Occupation.SALARIED);
        profile.setStudent(student);
        profile.setFarmer(false);
        profile.setHasDisability(false);
        profiles.saveAndFlush(profile);
        return user;
    }
}
