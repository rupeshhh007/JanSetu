package com.jansetu.backend.preparation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.profile.entity.*;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.policy.registry.PolicyVersionRepository;
import com.jansetu.backend.policy.registry.PolicyVersionStatus;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationPreparationIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired CitizenProfileRepository profiles;
    @Autowired SchemeRepository schemes;
    @Autowired PolicyVersionRepository policyVersions;
    @Autowired ApplicationPreparationSessionRepository sessions;
    @Autowired ApplicationPreparationHistoryRepository history;

    private User citizen;
    private User otherCitizen;
    private Scheme scheme;

    @BeforeEach
    void setUp() {
        citizen = citizen("preparation-owner");
        otherCitizen = citizen("preparation-other");
        scheme = new Scheme();
        scheme.setName("Preparation fixture " + UUID.randomUUID());
        scheme.setDescription("Synthetic preparation test scheme.");
        scheme.setState("Maharashtra"); scheme.setDepartment("Test Department");
        scheme.setOfficialUrl("https://example.gov.in/apply"); scheme.setSourceName("Synthetic authority");
        scheme.setStatus(SchemeStatus.PUBLISHED); scheme.setPublishedAt(Instant.now());
        EligibilityCriteria criteria = new EligibilityCriteria(); criteria.setMaxFamilyIncome(new BigDecimal("500000"));
        scheme.setEligibilityCriteria(criteria); scheme.setBenefits(List.of("Test support"));
        scheme.setRequiredDocuments(List.of("Income certificate"));
        scheme = schemes.saveAndFlush(scheme);
    }

    @Test
    void citizenCompletesPersistedPreparationAndReportsOnlyExternalApplication() throws Exception {
        JsonNode started = start(citizen);
        UUID sessionId = UUID.fromString(started.get("id").asText());
        JsonNode document = findTask(started, "DOCUMENT");
        assertEquals("PREPARING", started.get("status").asText());
        org.junit.jupiter.api.Assertions.assertTrue(started.get("progressPercent").asInt() < 100);

        mockMvc.perform(patch("/api/v1/preparations/{sessionId}/tasks/{taskId}", sessionId, document.get("id").asText())
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("JANSETU_PREPARATION_READY"))
                .andExpect(jsonPath("$.progressPercent").value(100));

        mockMvc.perform(post("/api/v1/preparations/{sessionId}/report-applied", sessionId).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CITIZEN_REPORTED_APPLICATION"))
                .andExpect(jsonPath("$.officialApplicationStatus").doesNotExist())
                .andExpect(jsonPath("$.citizenReportedAppliedAt").exists());

        mockMvc.perform(get("/api/v1/preparations/{sessionId}", sessionId).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.history.length()").value(4));
        assertEquals(1, sessions.count());
        assertEquals(4, history.findBySessionIdOrderByRecordedAtDesc(sessionId).size());
    }

    @Test
    void ownershipIsolationHidesAnotherCitizensJourney() throws Exception {
        UUID sessionId = UUID.fromString(start(citizen).get("id").asText());
        mockMvc.perform(get("/api/v1/preparations/{sessionId}", sessionId).with(user(otherCitizen)))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/v1/preparations/{sessionId}/tasks/{taskId}", sessionId, UUID.randomUUID())
                        .with(user(otherCitizen)).contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void incompleteJourneyCannotBeReportedAsApplied() throws Exception {
        UUID sessionId = UUID.fromString(start(citizen).get("id").asText());
        mockMvc.perform(post("/api/v1/preparations/{sessionId}/report-applied", sessionId).with(user(citizen)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Complete every known")));
    }

    @Test
    void changedSchemeRequirementsRefreshWithoutLosingCompletedDocument() throws Exception {
        JsonNode started = start(citizen);
        UUID sessionId = UUID.fromString(started.get("id").asText());
        String taskId = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{sessionId}/tasks/{taskId}", sessionId, taskId)
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("JANSETU_PREPARATION_READY"));

        scheme.setRequiredDocuments(List.of("Income certificate", "Residence certificate"));
        schemes.saveAndFlush(scheme);
        mockMvc.perform(get("/api/v1/preparations/{sessionId}", sessionId).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PREPARING"))
                .andExpect(jsonPath("$.tasks[?(@.type == 'DOCUMENT')]", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.tasks[?(@.title == 'Income certificate')].status")
                        .value(org.hamcrest.Matchers.contains("COMPLETE")))
                .andExpect(jsonPath("$.tasks[?(@.title == 'Residence certificate')].status")
                        .value(org.hamcrest.Matchers.contains("INCOMPLETE")));
    }

    @Test
    void nonEditableEligibilityTaskDirectsCitizenToProfile() throws Exception {
        JsonNode started = start(citizen);
        UUID sessionId = UUID.fromString(started.get("id").asText());
        JsonNode eligibilityTask = findTask(started, "ELIGIBILITY");
        mockMvc.perform(patch("/api/v1/preparations/{sessionId}/tasks/{taskId}", sessionId, eligibilityTask.get("id").asText())
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON).content("{\"complete\":false}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Update your profile")));
    }

    @Test
    void reportedExternalApplicationFreezesPreparationTaskMutations() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String document = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document).with(user(citizen))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/preparations/{id}/report-applied", id).with(user(citizen)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document).with(user(citizen))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"complete\":false}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/preparations/{id}", id).with(user(citizen)))
                .andExpect(jsonPath("$.status").value("CITIZEN_REPORTED_APPLICATION"))
                .andExpect(jsonPath("$.history.length()").value(4));
    }

    @Test
    void missingOfficialHandoffRemainsUnknownDespiteCompletedDocuments() throws Exception {
        scheme.setOfficialUrl(null);
        schemes.saveAndFlush(scheme);
        JsonNode started = json.readTree(mockMvc.perform(
                        post("/api/v1/preparations/schemes/{schemeId}", scheme.getId()).with(user(citizen)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.officialUrl").doesNotExist())
                .andReturn().getResponse().getContentAsString());
        UUID id = UUID.fromString(started.get("id").asText());
        String document = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document).with(user(citizen))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PREPARING"))
                .andExpect(jsonPath("$.tasks[?(@.title == 'Confirm the official application portal')].status")
                        .value(org.hamcrest.Matchers.contains("UNKNOWN")));
    }

    @Test
    void archivedSchemeCausesPreparationReadinessToBeWithdrawn() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String document = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document).with(user(citizen))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(jsonPath("$.status").value("JANSETU_PREPARATION_READY"));
        scheme.setStatus(SchemeStatus.ARCHIVED);
        schemes.saveAndFlush(scheme);
        mockMvc.perform(get("/api/v1/preparations/{id}", id).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PREPARING"));
        mockMvc.perform(post("/api/v1/preparations/{id}/report-applied", id).with(user(citizen)))
                .andExpect(status().isConflict());
    }

    @Test
    void profileFactChangeRecomputesReadinessWithoutSchemeRevision() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String document = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document).with(user(citizen))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(jsonPath("$.status").value("JANSETU_PREPARATION_READY"));
        CitizenProfile changed = profiles.findByUser(citizen).orElseThrow();
        changed.setAnnualFamilyIncome(new BigDecimal("999999"));
        profiles.saveAndFlush(changed);
        mockMvc.perform(get("/api/v1/preparations/{id}", id).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PREPARING"))
                .andExpect(jsonPath("$.progressPercent", org.hamcrest.Matchers.lessThan(100)));
    }

    @Test
    void unspecifiedDocumentRequirementsMustStayUnknown() throws Exception {
        scheme.setRequiredDocuments(List.of());
        schemes.saveAndFlush(scheme);
        start(citizen);
        mockMvc.perform(get("/api/v1/preparations").with(user(citizen)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PREPARING"))
                .andExpect(jsonPath("$[0].tasks[?(@.type == 'UNKNOWN_REQUIREMENT')].status")
                        .value(org.hamcrest.Matchers.contains("UNKNOWN")));
    }

    @Test
    void preparationUsesPublishedPolicyInsteadOfOutdatedLegacyCatalogueCriteria() throws Exception {
        PolicyVersion current = new PolicyVersion();
        current.setScheme(scheme);
        current.setVersionNumber(1);
        current.setStatus(PolicyVersionStatus.PUBLISHED);
        current.setEffectiveFrom(LocalDate.now().minusDays(1));
        current.setRecordedAt(Instant.now());
        current.setPublishedAt(Instant.now());
        current.setPolicyRule(new BooleanMatchRule("student-status", "Student status", FactKey.IS_STUDENT, false));
        current.setSourceReferences(List.of());
        policyVersions.saveAndFlush(current);

        JsonNode started = start(citizen);
        assertEquals("PREPARING", started.get("status").asText());
        org.junit.jupiter.api.Assertions.assertEquals(current.getId().toString(),
                started.get("policyVersionId").asText());
        mockMvc.perform(get("/api/v1/preparations/{id}", started.get("id").asText()).with(user(citizen)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[?(@.title == 'Published policy eligibility')].status")
                        .value(org.hamcrest.Matchers.contains("INCOMPLETE")));
    }

    @Test
    void repeatJourneyStartReturnsSameSessionWithoutDuplicateHistory() throws Exception {
        JsonNode original = start(citizen);
        JsonNode repeated = start(citizen);
        assertEquals(original.get("id").asText(), repeated.get("id").asText());
        assertEquals(1, sessions.count());
        assertEquals(1, history.findBySessionIdOrderByRecordedAtDesc(
                UUID.fromString(original.get("id").asText())).size());
    }

    @Test
    void duplicateDocumentNamesNeverCreateDuplicatePreparationTasks() throws Exception {
        scheme.setRequiredDocuments(List.of("Income certificate", "income certificate"));
        schemes.saveAndFlush(scheme);
        JsonNode started = start(citizen);
        mockMvc.perform(get("/api/v1/preparations/{id}", started.get("id").asText()).with(user(citizen)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[?(@.type == 'DOCUMENT')]", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void catalogueRefreshPreservesDocumentCompletionInstant() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String taskId = findTask(started, "DOCUMENT").get("id").asText();
        String result = mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, taskId)
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String completedAt = findTask(json.readTree(result), "DOCUMENT").get("completedAt").asText();
        scheme.setRequiredDocuments(List.of("Income certificate", "Residence certificate"));
        schemes.saveAndFlush(scheme);
        String updated = mockMvc.perform(get("/api/v1/preparations/{id}", id).with(user(citizen)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        for (JsonNode task : json.readTree(updated).get("tasks")) {
            if ("Income certificate".equals(task.get("title").asText())) {
                assertEquals(completedAt, task.get("completedAt").asText());
                return;
            }
        }
        throw new AssertionError("Previously completed document is missing after refresh");
    }

    @Test
    void nullCompletionFlagIsRejectedBeforeMutatingTasks() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String document = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document)
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/preparations/{id}", id).with(user(citizen)))
                .andExpect(jsonPath("$.tasks[?(@.type == 'DOCUMENT')].status")
                        .value(org.hamcrest.Matchers.contains("INCOMPLETE")));
    }

    @Test
    void listedJourneysArePrivateToTheAuthenticatedCitizen() throws Exception {
        start(citizen);
        mockMvc.perform(get("/api/v1/preparations").with(user(otherCitizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/preparations").with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void reportedApplicationKeepsItsHistoricalTasksAfterSchemeRevision() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String document = findTask(started, "DOCUMENT").get("id").asText();
        mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, document)
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON).content("{\"complete\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/preparations/{id}/report-applied", id).with(user(citizen)))
                .andExpect(status().isOk());
        scheme.setRequiredDocuments(List.of("A new post-application document"));
        schemes.saveAndFlush(scheme);
        mockMvc.perform(get("/api/v1/preparations/{id}", id).with(user(citizen)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CITIZEN_REPORTED_APPLICATION"))
                .andExpect(jsonPath("$.tasks[?(@.title == 'Income certificate')]", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.history.length()").value(4));
    }

    @Test
    void staleDocumentIdStillResolvesAfterRequirementsRefresh() throws Exception {
        JsonNode started = start(citizen);
        UUID id = UUID.fromString(started.get("id").asText());
        String originalId = findTask(started, "DOCUMENT").get("id").asText();
        scheme.setRequiredDocuments(List.of("Income certificate", "Residence certificate"));
        schemes.saveAndFlush(scheme);

        String updated = mockMvc.perform(patch("/api/v1/preparations/{id}/tasks/{taskId}", id, originalId)
                        .with(user(citizen)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"complete\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"))
                .andExpect(jsonPath("$.tasks[?(@.type == 'DOCUMENT')]", org.hamcrest.Matchers.hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        boolean retainedOriginalId = false;
        for (JsonNode task : json.readTree(updated).get("tasks")) {
            if ("Income certificate".equals(task.get("title").asText())) {
                retainedOriginalId = originalId.equals(task.get("id").asText());
                assertEquals("COMPLETE", task.get("status").asText());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(retainedOriginalId, "Stable document keys must keep task UUIDs");
    }

    private JsonNode start(User user) throws Exception {
        String body = mockMvc.perform(post("/api/v1/preparations/schemes/{schemeId}", scheme.getId()).with(user(user)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.officialUrl").value("https://example.gov.in/apply"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private JsonNode findTask(JsonNode response, String type) {
        for (JsonNode task : response.get("tasks")) if (type.equals(task.get("type").asText())) return task;
        throw new AssertionError("Missing task type " + type);
    }

    private User citizen(String prefix) {
        User user = new User(); user.setEmail(prefix + "-" + UUID.randomUUID() + "@jansetu.test");
        user.setPasswordHash("not-used"); user.setRole(Role.CITIZEN); user.setEnabled(true); user = users.saveAndFlush(user);
        CitizenProfile profile = new CitizenProfile(); profile.setUser(user); profile.setFirstName("Test"); profile.setLastName("Citizen");
        profile.setDateOfBirth(LocalDate.of(1995, 1, 1)); profile.setGender(Gender.FEMALE); profile.setState("Maharashtra");
        profile.setDistrict("Pune"); profile.setPincode("411001"); profile.setCategory(Category.GENERAL);
        profile.setAnnualFamilyIncome(new BigDecimal("100000")); profile.setMaritalStatus(MaritalStatus.SINGLE);
        profile.setPrimaryOccupation(Occupation.STUDENT); profile.setStudent(true); profile.setFarmer(false); profile.setHasDisability(false);
        profiles.saveAndFlush(profile); return user;
    }
}
