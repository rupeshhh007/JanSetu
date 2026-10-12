package com.jansetu.backend.policy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.assessment.PolicyAssessmentRepository;
import com.jansetu.backend.assessment.PolicyAssessmentService;
import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditRecordRepository;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.notification.CitizenPolicyNotificationRepository;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.registry.PolicyAdministrationService;
import com.jansetu.backend.policy.registry.PolicyLifecycleException;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionCreateCommand;
import com.jansetu.backend.policy.registry.PolicyVersionRepository;
import com.jansetu.backend.policy.registry.PolicyVersionService;
import com.jansetu.backend.policy.registry.PolicyVersionStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolicyPublicationWorkflowIntegrationTest {
    @Autowired private PolicyVersionService policies;
    @Autowired private PolicyAdministrationService administration;
    @Autowired private PolicyVersionRepository policyVersions;
    @Autowired private PolicyAssessmentService assessments;
    @Autowired private PolicyAssessmentRepository assessmentRecords;
    @Autowired private CitizenPolicyNotificationRepository notifications;
    @Autowired private AdminAuditRecordRepository audit;
    @Autowired private UserRepository users;
    @Autowired private CitizenProfileRepository profiles;
    @Autowired private SchemeRepository schemes;
    @Autowired private OfficialSourceRepository sources;
    @Autowired private SourceSnapshotRepository snapshots;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private User administrator;
    private User citizen;
    private User otherCitizen;
    private Scheme scheme;
    private OfficialSource source;

    @BeforeEach
    void setUp() {
        administrator = userAccount(Role.ADMIN);
        citizen = citizen(true);
        otherCitizen = citizen(false);
        scheme = scheme();
        source = source();

        PolicyVersion initial = policies.createDraft(scheme.getId(), command(LocalDate.of(2026, 1, 1), true));
        policies.submitForReview(scheme.getId(), initial.getId());
        policies.approve(scheme.getId(), initial.getId());
        policies.publish(scheme.getId(), initial.getId());
        assessments.assessCurrent(citizen, scheme.getId());
        assessments.assessCurrent(otherCitizen, scheme.getId());
    }

    @Test
    void publicationReassessesCitizensNotifiesThemAndWritesAnAuditTrailAtomically() throws Exception {
        PolicyVersion draft = policies.createDraft(scheme.getId(), command(LocalDate.of(2026, 11, 1), false));

        administration.submitForReview(scheme.getId(), draft.getId(), "Evidence was linked and checked.", administrator);
        administration.approve(scheme.getId(), draft.getId(), "Independent policy review passed.", administrator);
        administration.publish(scheme.getId(), draft.getId(), "Publish the reviewed replacement.", administrator);

        PolicyVersion published = policyVersions.findById(draft.getId()).orElseThrow();
        assertEquals(PolicyVersionStatus.PUBLISHED, published.getStatus());
        assertEquals(4, assessmentRecords.count());
        assertEquals(2, notifications.count());
        var events = audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("POLICY_VERSION", draft.getId());
        assertEquals(List.of(AdminAuditAction.POLICY_PUBLISHED, AdminAuditAction.POLICY_APPROVED,
                AdminAuditAction.POLICY_SUBMITTED_FOR_REVIEW), events.stream().map(event -> event.getAction()).toList());
        assertEquals("2", events.getFirst().getMetadata().get("reassessedCitizens"));

        String response = mockMvc.perform(get("/api/v1/notifications").with(user(citizen)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].schemeId").value(scheme.getId().toString()))
                .andExpect(jsonPath("$[0].previousResult").value(PolicyResult.SATISFIED.name()))
                .andExpect(jsonPath("$[0].currentResult").value(PolicyResult.FAILED.name()))
                .andExpect(jsonPath("$[0].readAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        UUID notificationId = UUID.fromString(objectMapper.readTree(response).get(0).get("id").asText());

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.readAt").exists());
        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId).with(user(otherCitizen)))
                .andExpect(status().isNotFound());

        long assessmentCount = assessmentRecords.count();
        long notificationCount = notifications.count();
        long auditCount = audit.count();
        assertThrows(PolicyLifecycleException.class, () -> administration.publish(
                scheme.getId(), draft.getId(), "Attempt duplicate publication.", administrator));
        assertEquals(assessmentCount, assessmentRecords.count());
        assertEquals(notificationCount, notifications.count());
        assertEquals(auditCount, audit.count());
    }

    @Test
    void rejectedAdministrationRequestsDoNotChangeLifecycleOrAuditEvidence() throws Exception {
        PolicyVersion draft = policies.createDraft(scheme.getId(), command(LocalDate.of(2026, 12, 1), false));
        String url = "/api/v1/admin/schemes/" + scheme.getId() + "/policy-versions/" + draft.getId() + "/submit-review";

        mockMvc.perform(post(url).with(user(citizen)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Citizen cannot administer policy.\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(url).with(user(administrator)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest());

        assertThrows(PolicyLifecycleException.class, () -> administration.submitForReview(
                scheme.getId(), draft.getId(), "Valid-looking citizen request.", citizen));
        assertEquals(PolicyVersionStatus.DRAFT, policyVersions.findById(draft.getId()).orElseThrow().getStatus());
        assertEquals(0, audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("POLICY_VERSION", draft.getId()).size());
    }

    private PolicyVersionCreateCommand command(LocalDate effectiveFrom, boolean expectedStudent) {
        String marker = "policy-" + effectiveFrom;
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSource(source);
        snapshot.setSourceNameAtCapture(source.getName());
        snapshot.setSourceUrlAtCapture(source.getCanonicalUrl());
        snapshot.setContentType("text/plain");
        snapshot.setHttpStatus(200);
        snapshot.setContentLength(marker.length());
        snapshot.setContentHash("sha256:" + marker);
        snapshot.setContentExcerpt("Official evidence for " + effectiveFrom);
        snapshot.setContentBytes(marker.getBytes());
        snapshot.setCapturedAt(Instant.parse("2026-10-01T00:00:00Z"));
        snapshot = snapshots.saveAndFlush(snapshot);
        return new PolicyVersionCreateCommand(effectiveFrom,
                new BooleanMatchRule("student-rule", "Student eligibility", FactKey.IS_STUDENT, expectedStudent),
                List.of(new PolicySourceReference(snapshot.getId(), null, null, null, null)));
    }

    private User userAccount(Role role) {
        User user = new User();
        user.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@jansetu.test");
        user.setPasswordHash("not-used-by-test");
        user.setRole(role);
        user.setEnabled(true);
        return users.saveAndFlush(user);
    }

    private User citizen(boolean student) {
        User user = userAccount(Role.CITIZEN);
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

    private Scheme scheme() {
        Scheme value = new Scheme();
        value.setName("Publication workflow " + UUID.randomUUID());
        value.setDescription("Controlled publication fixture.");
        value.setState("Maharashtra");
        value.setDepartment("Test department");
        value.setStatus(SchemeStatus.PUBLISHED);
        value.setEligibilityCriteria(new EligibilityCriteria());
        value.setBenefits(List.of());
        value.setRequiredDocuments(List.of());
        return schemes.saveAndFlush(value);
    }

    private OfficialSource source() {
        OfficialSource value = new OfficialSource();
        value.setName("Official workflow fixture");
        value.setAuthority("Government of Maharashtra");
        value.setCanonicalUrl("https://mahadbt.maharashtra.gov.in/workflow/" + UUID.randomUUID());
        value.setNormalizedHost("mahadbt.maharashtra.gov.in");
        return sources.saveAndFlush(value);
    }
}
