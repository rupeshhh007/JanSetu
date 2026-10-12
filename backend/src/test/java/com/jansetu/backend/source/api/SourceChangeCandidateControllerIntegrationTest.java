package com.jansetu.backend.source.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditRecordRepository;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.OfficialSourceRepository;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.SourceSnapshotRepository;
import com.jansetu.backend.source.change.SourceChangeCandidate;
import com.jansetu.backend.source.change.SourceChangeCandidateRepository;
import com.jansetu.backend.source.change.SourceChangeCandidateStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SourceChangeCandidateControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository users;
    @Autowired private SchemeRepository schemes;
    @Autowired private OfficialSourceRepository sources;
    @Autowired private SourceSnapshotRepository snapshots;
    @Autowired private SourceChangeCandidateRepository candidates;
    @Autowired private AdminAuditRecordRepository audit;

    private User admin;
    private UUID schemeId;
    private UUID candidateId;

    @BeforeEach
    void setUp() {
        admin = user("reviewer-" + UUID.randomUUID() + "@jansetu.test", Role.ADMIN);
        Scheme scheme = new Scheme();
        scheme.setName("Review fixture " + UUID.randomUUID());
        scheme.setDescription("Synthetic scheme used only for source-review integration tests.");
        scheme.setState("Maharashtra");
        scheme.setDepartment("Test");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setEligibilityCriteria(new EligibilityCriteria());
        scheme.setBenefits(List.of());
        scheme.setRequiredDocuments(List.of());
        schemeId = schemes.saveAndFlush(scheme).getId();
        candidateId = candidate().getId();
    }

    @Test
    void citizenCannotReadOrReviewTheAdministrativeQueue() throws Exception {
        User citizen = user("citizen-" + UUID.randomUUID() + "@jansetu.test", Role.CITIZEN);
        mockMvc.perform(get(base()).with(SecurityMockMvcRequestPostProcessors.user(citizen)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(base() + "/{id}/accept", candidateId)
                        .with(SecurityMockMvcRequestPostProcessors.user(citizen))
                        .contentType(MediaType.APPLICATION_JSON).content(json(schemeId, "Relevant source evidence.")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorAcceptsARelevantCandidateAndCreatesOneAuditRecord() throws Exception {
        mockMvc.perform(post(base() + "/{id}/accept", candidateId).with(SecurityMockMvcRequestPostProcessors.user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(schemeId, "Income threshold changed in the recorded source.")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPLICABLE"))
                .andExpect(jsonPath("$.schemeId").value(schemeId.toString()));

        assertEquals(SourceChangeCandidateStatus.APPLICABLE, candidates.findById(candidateId).orElseThrow().getStatus());
        var records = audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("SOURCE_CHANGE_CANDIDATE", candidateId);
        assertEquals(1, records.size());
        assertEquals(AdminAuditAction.SOURCE_CHANGE_ACCEPTED, records.getFirst().getAction());
    }

    @Test
    void duplicateOrInvalidReviewCannotCreateSuccessAuditEvidence() throws Exception {
        mockMvc.perform(post(base() + "/{id}/reject", candidateId).with(SecurityMockMvcRequestPostProcessors.user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(null, "Source change is not applicable to JanSetu policy.")))
                .andExpect(status().isOk());
        mockMvc.perform(post(base() + "/{id}/accept", candidateId).with(SecurityMockMvcRequestPostProcessors.user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(schemeId, "A later reviewer cannot overwrite this decision.")))
                .andExpect(status().isConflict());
        assertEquals(1, audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("SOURCE_CHANGE_CANDIDATE", candidateId).size());
        assertEquals(AdminAuditAction.SOURCE_CHANGE_REJECTED,
                audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("SOURCE_CHANGE_CANDIDATE", candidateId).getFirst().getAction());
    }

    @Test
    void missingReasonOrUnknownSchemeLeavesCandidatePendingWithoutAudit() throws Exception {
        mockMvc.perform(post(base() + "/{id}/accept", candidateId).with(SecurityMockMvcRequestPostProcessors.user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(schemeId, "")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(base() + "/{id}/accept", candidateId).with(SecurityMockMvcRequestPostProcessors.user(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(json(UUID.randomUUID(), "Scheme mapping must resolve.")))
                .andExpect(status().isNotFound());
        assertEquals(SourceChangeCandidateStatus.PENDING_REVIEW, candidates.findById(candidateId).orElseThrow().getStatus());
        assertEquals(0, audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("SOURCE_CHANGE_CANDIDATE", candidateId).size());
    }

    private SourceChangeCandidate candidate() {
        OfficialSource source = new OfficialSource();
        source.setName("Source review fixture"); source.setAuthority("Test authority");
        source.setCanonicalUrl("https://example.gov.in/fixture/" + UUID.randomUUID()); source.setNormalizedHost("example.gov.in");
        source = sources.saveAndFlush(source);
        SourceSnapshot previous = snapshot(source, "sha256:before", "Income threshold 200000");
        SourceSnapshot current = snapshot(source, "sha256:after", "Income threshold 300000");
        SourceChangeCandidate candidate = new SourceChangeCandidate();
        candidate.setSource(source); candidate.setPreviousSnapshot(previous); candidate.setNewSnapshot(current);
        candidate.setChangeSummary("Meaningful synthetic source change for integration testing.");
        return candidates.saveAndFlush(candidate);
    }

    private SourceSnapshot snapshot(OfficialSource source, String hash, String text) {
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSource(source); snapshot.setSourceNameAtCapture(source.getName()); snapshot.setSourceUrlAtCapture(source.getCanonicalUrl());
        snapshot.setContentType("text/plain"); snapshot.setHttpStatus(200); snapshot.setContentLength(text.length());
        snapshot.setContentHash(hash); snapshot.setContentExcerpt(text); snapshot.setContentBytes(text.getBytes(StandardCharsets.UTF_8));
        snapshot.setCapturedAt(Instant.now());
        return snapshots.saveAndFlush(snapshot);
    }

    private User user(String email, Role role) {
        User user = new User(); user.setEmail(email); user.setPasswordHash("not-used-in-mockmvc"); user.setRole(role); user.setEnabled(true);
        return users.saveAndFlush(user);
    }
    private String base() { return "/api/v1/admin/source-change-candidates"; }
    private String json(UUID scheme, String reason) throws Exception { return objectMapper.writeValueAsString(new SourceChangeReviewRequest(scheme, reason)); }
}
