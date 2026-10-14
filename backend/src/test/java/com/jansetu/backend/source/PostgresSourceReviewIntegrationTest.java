package com.jansetu.backend.source;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.audit.AdminAuditRecordRepository;
import com.jansetu.backend.agent.domain.AgentRunStatus;
import com.jansetu.backend.agent.persistence.PolicyAgentExecutionEventRepository;
import com.jansetu.backend.agent.persistence.PolicyAgentRunRepository;
import com.jansetu.backend.agent.service.AgentExecutionService;
import com.jansetu.backend.agent.service.PolicyInvestigationException;
import com.jansetu.backend.agent.service.PolicyInvestigationReviewService;
import com.jansetu.backend.agent.api.AgentReviewDecision;
import com.jansetu.backend.agent.api.PolicyInvestigationReviewRequest;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.policy.registry.PolicyVersionRepository;
import com.jansetu.backend.preparation.ApplicationPreparationHistoryRepository;
import com.jansetu.backend.preparation.ApplicationPreparationSessionRepository;
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
import com.jansetu.backend.source.change.SourceChangeCandidate;
import com.jansetu.backend.source.change.SourceChangeCandidateRepository;
import com.jansetu.backend.source.change.SourceChangeCandidateService;
import com.jansetu.backend.source.change.SourceChangeCandidateStatus;
import com.jansetu.backend.source.api.SourceChangeReviewRequest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = false)
@SpringBootTest
@AutoConfigureMockMvc
class PostgresSourceReviewIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired OfficialSourceRepository sources;
    @Autowired SourceSnapshotRepository snapshots;
    @Autowired SourceChangeCandidateRepository candidates;
    @Autowired SourceChangeCandidateService review;
    @Autowired SchemeRepository schemes;
    @Autowired UserRepository users;
    @Autowired AdminAuditRecordRepository audit;
    @Autowired PolicyVersionRepository policyVersions;
    @Autowired CitizenProfileRepository profiles;
    @Autowired ApplicationPreparationSessionRepository preparations;
    @Autowired ApplicationPreparationHistoryRepository preparationHistory;
    @Autowired AgentExecutionService agentExecutions;
    @Autowired PolicyInvestigationReviewService agentReviews;
    @Autowired PolicyAgentRunRepository agentRuns;
    @Autowired PolicyAgentExecutionEventRepository agentEvents;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;

    @Test
    void appliesAllMigrationsAndPersistsDetectedReviewWithoutPublishingPolicy() {
        assertEquals("17", flyway.info().current().getVersion().getVersion());
        assertEquals("integer", columnType("users", "token_version"));
        assertEquals(1, indexCount("citizen_saved_schemes", "idx_citizen_saved_schemes_%"));
        assertEquals(3, indexCount("source_change_candidates", "idx_source_change_candidates_%"));
        assertEquals(2, indexCount("administrative_audit_records", "idx_administrative_audit_records_%"));
        assertEquals(1, indexCount("citizen_policy_notifications", "idx_citizen_policy_notifications_%"));
        assertEquals("bigint", columnType("source_change_candidates", "revision"));
        assertEquals(7, foreignKeyCount("source_change_candidates"));
        assertEquals(3, foreignKeyCount("citizen_policy_notifications"));
        assertEquals("character varying", columnType("citizen_policy_notifications", "current_result"));
        assertEquals(3, indexCount("policy_agent_runs", "idx_policy_agent_runs_%"));
        assertEquals(1, indexCount("policy_agent_execution_events", "idx_policy_agent_events_%"));
        assertEquals(4, foreignKeyCount("policy_agent_runs"));
        assertEquals("jsonb", columnType("policy_agent_runs", "result"));
        assertEquals("integer", columnType("policy_agent_runs", "estimated_tokens_used"));
        assertEquals("character varying", columnType("policy_agent_runs", "review_status"));
        assertEquals(30, columnLength("policy_agent_runs", "execution_mode"));
        assertEquals(1, indexCount("application_preparation_sessions", "idx_preparation_sessions_%"));
        assertEquals(1, indexCount("application_preparation_tasks", "idx_preparation_tasks_%"));
        assertEquals(1, indexCount("application_preparation_history", "idx_preparation_history_%"));
        assertEquals("jsonb", columnType("application_preparation_tasks", "evidence_references"));

        Fixture fixture = fixture();
        long policyCount = policyVersions.count();
        SourceChangeCandidate reviewed = review.accept(fixture.candidateId(), fixture.schemeId(),
                "Verified controlled fixture change.", fixture.admin());

        assertEquals(SourceChangeCandidateStatus.APPLICABLE, reviewed.getStatus());
        assertEquals(1, auditFor(fixture.candidateId()));
        assertEquals(policyCount, policyVersions.count(), "source review must not create or publish a policy version");

        TransactionTemplate transaction = new TransactionTemplate(transactions);
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored ->
                jdbc.update("update administrative_audit_records set decision_reason = 'rewritten' where entity_id = ?",
                        fixture.candidateId())));
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored ->
                jdbc.update("delete from administrative_audit_records where entity_id = ?", fixture.candidateId())));
        assertEquals(1, auditFor(fixture.candidateId()));
    }

    @Test
    void agentRunAndAppendOnlyToolTimelinePersistOnPostgres() {
        Fixture fixture = fixture();
        var run = agentExecutions.start(fixture.candidateId(), fixture.schemeId(),
                "postgres-agent-" + UUID.randomUUID(), fixture.admin());

        assertEquals(AgentRunStatus.COMPLETED, run.getStatus());
        assertEquals(10, run.getToolCallCount());
        assertEquals(12, agentEvents.countByRunId(run.getId()));
        assertTrue(agentRuns.existsById(run.getId()));
        assertEquals(0, policyVersions.count(), "an investigation must not create or publish a policy");

        UUID eventId = agentEvents.findByRunIdOrderBySequenceNumber(run.getId()).getFirst().getId();
        TransactionTemplate transaction = new TransactionTemplate(transactions);
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored ->
                jdbc.update("update policy_agent_execution_events set outcome = 'ERROR' where id = ?", eventId)));
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored ->
                jdbc.update("delete from policy_agent_execution_events where id = ?", eventId)));
        assertEquals(12, agentEvents.countByRunId(run.getId()));
    }

    @Test
    void concurrentIdenticalAgentStartsReturnTheSinglePersistedRun() throws Exception {
        Fixture fixture = fixture();
        String key = "postgres-replay-" + UUID.randomUUID();
        CyclicBarrier startTogether = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<UUID> first = executor.submit(() -> {
                startTogether.await();
                return agentExecutions.start(fixture.candidateId(), fixture.schemeId(), key, fixture.admin()).getId();
            });
            Future<UUID> second = executor.submit(() -> {
                startTogether.await();
                return agentExecutions.start(fixture.candidateId(), fixture.schemeId(), key, fixture.admin()).getId();
            });
            UUID firstRunId = first.get();
            assertEquals(firstRunId, second.get(), "competing submissions must replay one investigation");
            assertEquals(firstRunId, agentRuns.findByStartedByIdAndIdempotencyKey(fixture.admin().getId(), key)
                    .orElseThrow().getId());
            assertEquals(12, agentEvents.countByRunId(firstRunId));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void competingAgentReviewsCommitOneDecisionAndOneAudit() throws Exception {
        Fixture fixture = fixture();
        var run = agentExecutions.start(fixture.candidateId(), fixture.schemeId(),
                "postgres-agent-review-" + UUID.randomUUID(), fixture.admin());
        CyclicBarrier startTogether = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> approved = executor.submit(() -> agentReview(startTogether, run.getId(), fixture.admin(), true));
            Future<Boolean> rejected = executor.submit(() -> agentReview(startTogether, run.getId(), fixture.admin(), false));

            assertEquals(1, List.of(approved.get(), rejected.get()).stream().filter(Boolean::booleanValue).count());
            assertEquals(1, audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("POLICY_AGENT_RUN", run.getId()).size());
            assertTrue(agentRuns.findById(run.getId()).orElseThrow().getReviewStatus().name().matches("APPROVED|REJECTED"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void competingAcceptAndRejectUseSeparateTransactionsAndCommitOneDecision() throws Exception {
        Fixture fixture = fixture();
        CyclicBarrier startTogether = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> accepted = executor.submit(() -> reviewRequest(startTogether, fixture, true));
            Future<Integer> rejected = executor.submit(() -> reviewRequest(startTogether, fixture, false));
            List<Integer> outcomes = List.of(accepted.get(), rejected.get());

            assertEquals(1, outcomes.stream().filter(status -> status == 200).count());
            assertEquals(1, outcomes.stream().filter(status -> status == 409).count());
            assertEquals(1, auditFor(fixture.candidateId()));
            SourceChangeCandidate stored = candidates.findById(fixture.candidateId()).orElseThrow();
            assertTrue(stored.getStatus() == SourceChangeCandidateStatus.APPLICABLE
                    || stored.getStatus() == SourceChangeCandidateStatus.REJECTED);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void auditConstraintFailureRollsBackCandidateDecision() {
        Fixture fixture = fixture();
        String oversizedAuditReason = "x".repeat(1001);
        TransactionTemplate transaction = new TransactionTemplate(transactions);

        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored -> {
            User admin = users.findById(fixture.admin().getId()).orElseThrow();
            review.accept(fixture.candidateId(), fixture.schemeId(), oversizedAuditReason, admin);
        }));

        assertEquals(SourceChangeCandidateStatus.PENDING_REVIEW,
                candidates.findById(fixture.candidateId()).orElseThrow().getStatus());
        assertEquals(0, auditFor(fixture.candidateId()));
    }

    @Test
    void authorizationAndCrossResourceValidationHoldAgainstPostgres() throws Exception {
        Fixture fixture = fixture();
        User citizen = user("postgres-citizen", Role.CITIZEN);
        mockMvc.perform(get("/api/v1/admin/source-change-candidates")
                        .with(SecurityMockMvcRequestPostProcessors.user(citizen)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/source-change-candidates")
                        .with(SecurityMockMvcRequestPostProcessors.user(fixture.admin())))
                .andExpect(status().isOk());

        TransactionTemplate transaction = new TransactionTemplate(transactions);
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored -> {
            User admin = users.findById(fixture.admin().getId()).orElseThrow();
            review.accept(fixture.candidateId(), UUID.randomUUID(), "Must resolve a real scheme.", admin);
        }));
        assertEquals(SourceChangeCandidateStatus.PENDING_REVIEW,
                candidates.findById(fixture.candidateId()).orElseThrow().getStatus());
        assertEquals(0, auditFor(fixture.candidateId()));
    }

    @Test
    void citizenPreparationJourneyPersistsWithAppendOnlyHistoryOnPostgres() throws Exception {
        User citizen = citizen("postgres-preparation");
        Scheme scheme = preparationScheme();

        var started = objectMapper.readTree(mockMvc.perform(post("/api/v1/preparations/schemes/{schemeId}", scheme.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(citizen)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        UUID sessionId = UUID.fromString(started.get("id").asText());
        UUID documentTaskId = null;
        for (var task : started.get("tasks")) {
            if ("DOCUMENT".equals(task.get("type").asText())) {
                documentTaskId = UUID.fromString(task.get("id").asText());
                break;
            }
        }
        assertTrue(documentTaskId != null, "the verified document requirement must become a task");

        mockMvc.perform(patch("/api/v1/preparations/{sessionId}/tasks/{taskId}", sessionId, documentTaskId)
                        .with(SecurityMockMvcRequestPostProcessors.user(citizen))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"complete\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/preparations/{sessionId}/report-applied", sessionId)
                        .with(SecurityMockMvcRequestPostProcessors.user(citizen)))
                .andExpect(status().isOk());

        assertEquals("CITIZEN_REPORTED_APPLICATION", preparations.findById(sessionId).orElseThrow().getStatus().name());
        assertEquals(4, preparationHistory.findBySessionIdOrderByRecordedAtDesc(sessionId).size());
        UUID historyId = preparationHistory.findBySessionIdOrderByRecordedAtDesc(sessionId).getFirst().getId();
        TransactionTemplate transaction = new TransactionTemplate(transactions);
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored ->
                jdbc.update("update application_preparation_history set event_type = 'TASK_REOPENED' where id = ?", historyId)));
        assertThrows(RuntimeException.class, () -> transaction.executeWithoutResult(ignored ->
                jdbc.update("delete from application_preparation_history where id = ?", historyId)));
        assertEquals(4, preparationHistory.findBySessionIdOrderByRecordedAtDesc(sessionId).size());
    }

    private int reviewRequest(CyclicBarrier barrier, Fixture fixture, boolean accept) throws Exception {
        barrier.await();
        String action = accept ? "accept" : "reject";
        SourceChangeReviewRequest request = new SourceChangeReviewRequest(
                accept ? fixture.schemeId() : null, accept ? "Concurrent accept." : "Concurrent reject.");
        return mockMvc.perform(post("/api/v1/admin/source-change-candidates/{id}/{action}", fixture.candidateId(), action)
                        .with(SecurityMockMvcRequestPostProcessors.user(fixture.admin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();
    }

    private boolean agentReview(CyclicBarrier barrier, UUID runId, User admin, boolean approve) throws Exception {
        barrier.await();
        try {
            agentReviews.review(runId, new PolicyInvestigationReviewRequest(
                    approve ? AgentReviewDecision.APPROVE : AgentReviewDecision.REJECT,
                    approve ? "Concurrent human approval." : "Concurrent human rejection.", null), admin);
            return true;
        } catch (PolicyInvestigationException expectedConflict) {
            return false;
        }
    }

    private Fixture fixture() {
        OfficialSource source = new OfficialSource();
        source.setName("Postgres source");
        source.setAuthority("Test authority");
        source.setCanonicalUrl("https://example.gov.in/postgres/" + UUID.randomUUID());
        source.setNormalizedHost("example.gov.in");
        source = sources.saveAndFlush(source);
        SourceSnapshot before = snapshot(source, "sha256:before-" + UUID.randomUUID(), "income below 200000");
        SourceSnapshot after = snapshot(source, "sha256:after-" + UUID.randomUUID(), "income below 300000");
        SourceChangeCandidate candidate = review.detect(before, after).orElseThrow();

        Scheme scheme = new Scheme();
        scheme.setName("Postgres scheme " + UUID.randomUUID());
        scheme.setDescription("fixture");
        scheme.setState("Maharashtra");
        scheme.setDepartment("Test");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setEligibilityCriteria(new EligibilityCriteria());
        scheme.setBenefits(List.of());
        scheme.setRequiredDocuments(List.of());
        scheme = schemes.saveAndFlush(scheme);
        return new Fixture(candidate.getId(), scheme.getId(), user("postgres-admin", Role.ADMIN));
    }

    private User user(String prefix, Role role) {
        User user = new User();
        user.setEmail(prefix + "-" + UUID.randomUUID() + "@jansetu.test");
        user.setPasswordHash("not-used");
        user.setRole(role);
        user.setEnabled(true);
        return users.saveAndFlush(user);
    }

    private User citizen(String prefix) {
        User user = user(prefix, Role.CITIZEN);
        CitizenProfile profile = new CitizenProfile();
        profile.setUser(user);
        profile.setFirstName("Postgres");
        profile.setLastName("Citizen");
        profile.setDateOfBirth(LocalDate.of(1995, 1, 1));
        profile.setGender(Gender.FEMALE);
        profile.setState("Maharashtra");
        profile.setDistrict("Pune");
        profile.setPincode("411001");
        profile.setCategory(Category.GENERAL);
        profile.setAnnualFamilyIncome(new BigDecimal("100000"));
        profile.setMaritalStatus(MaritalStatus.SINGLE);
        profile.setPrimaryOccupation(Occupation.STUDENT);
        profile.setStudent(true);
        profiles.saveAndFlush(profile);
        return user;
    }

    private Scheme preparationScheme() {
        Scheme scheme = new Scheme();
        scheme.setName("Postgres preparation " + UUID.randomUUID());
        scheme.setDescription("PostgreSQL application preparation fixture.");
        scheme.setState("Maharashtra");
        scheme.setDepartment("Test");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setPublishedAt(Instant.now());
        scheme.setOfficialUrl("https://example.gov.in/apply");
        scheme.setSourceName("Synthetic authority");
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setMaxFamilyIncome(new BigDecimal("500000"));
        scheme.setEligibilityCriteria(criteria);
        scheme.setBenefits(List.of("Test support"));
        scheme.setRequiredDocuments(List.of("Income certificate"));
        return schemes.saveAndFlush(scheme);
    }

    private SourceSnapshot snapshot(OfficialSource source, String hash, String text) {
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSource(source);
        snapshot.setSourceNameAtCapture(source.getName());
        snapshot.setSourceUrlAtCapture(source.getCanonicalUrl());
        snapshot.setContentType("text/plain");
        snapshot.setHttpStatus(200);
        snapshot.setContentLength(text.length());
        snapshot.setContentHash(hash);
        snapshot.setContentExcerpt(text);
        snapshot.setContentBytes(text.getBytes(StandardCharsets.UTF_8));
        snapshot.setCapturedAt(Instant.now());
        return snapshots.saveAndFlush(snapshot);
    }

    private int auditFor(UUID candidateId) {
        return audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("SOURCE_CHANGE_CANDIDATE", candidateId).size();
    }

    private int indexCount(String table, String namePattern) {
        Integer count = jdbc.queryForObject(
                "select count(*) from pg_indexes where tablename = ? and indexname like ?",
                Integer.class, table, namePattern);
        return count == null ? 0 : count;
    }

    private String columnType(String table, String column) {
        return jdbc.queryForObject(
                "select data_type from information_schema.columns where table_name = ? and column_name = ?",
                String.class, table, column);
    }

    private int columnLength(String table, String column) {
        Integer length = jdbc.queryForObject(
                "select character_maximum_length from information_schema.columns where table_name = ? and column_name = ?",
                Integer.class, table, column);
        return length == null ? 0 : length;
    }

    private int foreignKeyCount(String table) {
        Integer count = jdbc.queryForObject(
                "select count(*) from pg_constraint where contype = 'f' and conrelid = cast(? as regclass)",
                Integer.class, table);
        return count == null ? 0 : count;
    }

    private record Fixture(UUID candidateId, UUID schemeId, User admin) {}
}
