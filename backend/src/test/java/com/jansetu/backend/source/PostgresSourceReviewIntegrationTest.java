package com.jansetu.backend.source;

import com.jansetu.backend.audit.AdminAuditRecordRepository;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import com.jansetu.backend.source.change.SourceChangeCandidate;
import com.jansetu.backend.source.change.SourceChangeCandidateRepository;
import com.jansetu.backend.source.change.SourceChangeCandidateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = false)
@SpringBootTest
class PostgresSourceReviewIntegrationTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl); registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword); registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); registry.add("spring.flyway.enabled", () -> true);
    }
    @Autowired OfficialSourceRepository sources; @Autowired SourceSnapshotRepository snapshots;
    @Autowired SourceChangeCandidateRepository candidates; @Autowired SourceChangeCandidateService review;
    @Autowired SchemeRepository schemes; @Autowired UserRepository users; @Autowired AdminAuditRecordRepository audit;

    @Test void migratesAndPersistsReviewedCandidateWithAudit() {
        assertNotNull(sources);
        OfficialSource source = new OfficialSource(); source.setName("Postgres source"); source.setAuthority("Test"); source.setCanonicalUrl("https://example.gov.in/postgres"); source.setNormalizedHost("example.gov.in"); source = sources.saveAndFlush(source);
        SourceSnapshot before = snapshot(source, "sha256:before", "income below 200000"); SourceSnapshot after = snapshot(source, "sha256:after", "income below 300000");
        SourceChangeCandidate candidate = new SourceChangeCandidate(); candidate.setSource(source); candidate.setPreviousSnapshot(before); candidate.setNewSnapshot(after); candidate.setChangeSummary("PostgreSQL migration fixture"); candidate = candidates.saveAndFlush(candidate);
        Scheme scheme = new Scheme(); scheme.setName("Postgres scheme"); scheme.setDescription("fixture"); scheme.setState("Maharashtra"); scheme.setDepartment("Test"); scheme.setStatus(SchemeStatus.PUBLISHED); scheme.setEligibilityCriteria(new EligibilityCriteria()); scheme.setBenefits(List.of()); scheme.setRequiredDocuments(List.of()); scheme = schemes.saveAndFlush(scheme);
        User admin = new User(); admin.setEmail("postgres-admin@jansetu.test"); admin.setPasswordHash("not-used"); admin.setRole(Role.ADMIN); admin.setEnabled(true); admin = users.saveAndFlush(admin);
        review.accept(candidate.getId(), scheme.getId(), "Verified controlled fixture change.", admin);
        assertEquals(1, audit.findByEntityTypeAndEntityIdOrderByRecordedAtDesc("SOURCE_CHANGE_CANDIDATE", candidate.getId()).size());
    }
    private SourceSnapshot snapshot(OfficialSource source, String hash, String text) { SourceSnapshot s = new SourceSnapshot(); s.setSource(source); s.setSourceNameAtCapture(source.getName()); s.setSourceUrlAtCapture(source.getCanonicalUrl()); s.setContentType("text/plain"); s.setHttpStatus(200); s.setContentLength(text.length()); s.setContentHash(hash); s.setContentExcerpt(text); s.setContentBytes(text.getBytes(StandardCharsets.UTF_8)); s.setCapturedAt(Instant.now()); return snapshots.saveAndFlush(s); }
}
