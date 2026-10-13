package com.jansetu.backend.agent.service;

import com.jansetu.backend.agent.domain.AgentRunStatus;
import com.jansetu.backend.agent.domain.EvidenceCitation;
import com.jansetu.backend.agent.domain.PolicyInvestigationResult;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.validation.PolicyRuleValidator;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.SourceSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** No LLM or database required: evidence metadata must come from the immutable snapshot. */
class PolicyProposalValidatorTest {
    private final UUID snapshotId = UUID.randomUUID();
    private final UUID candidateId = UUID.randomUUID();
    private final UUID schemeId = UUID.randomUUID();
    private final SourceSnapshotRepository snapshots = mock(SourceSnapshotRepository.class);
    private final PolicyRuleValidator rules = mock(PolicyRuleValidator.class);
    private PolicyProposalValidator validator;

    @BeforeEach
    void setup() {
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSourceUrlAtCapture("https://india.gov.in/fixture");
        snapshot.setSourceNameAtCapture("Official fixture");
        snapshot.setContentHash("sha256:known");
        snapshot.setContentExcerpt("Income requirement is 300000.");
        snapshot.setContentBytes("Income requirement is 300000.".getBytes(StandardCharsets.UTF_8));
        when(snapshots.findById(snapshotId)).thenReturn(Optional.of(snapshot));
        when(rules.validate(any())).thenReturn(List.of());
        validator = new PolicyProposalValidator(rules, snapshots);
    }

    @Test
    void refusesForgedSourceUrlEvenWhenHashAndSpanMatch() {
        EvidenceCitation spoof = new EvidenceCitation(snapshotId, "Official fixture",
                "https://attacker.example/fixture", "sha256:known", "Income requirement is 300000.");
        assertTrue(validator.validate(proposal(List.of(spoof)), candidateId, schemeId, Set.of(snapshotId))
                .stream().anyMatch(issue -> issue.code().equals("EVIDENCE_URL_MISMATCH")));
    }

    @Test
    void refusesForgedSourceTitle() {
        EvidenceCitation spoof = new EvidenceCitation(snapshotId, "Different government agency",
                "https://india.gov.in/fixture", "sha256:known", "Income requirement is 300000.");
        assertTrue(validator.validate(proposal(List.of(spoof)), candidateId, schemeId, Set.of(snapshotId))
                .stream().anyMatch(issue -> issue.code().equals("EVIDENCE_TITLE_MISMATCH")));
    }

    @Test
    void refusesUnboundedCitationCount() {
        List<EvidenceCitation> eleven = java.util.stream.IntStream.range(0, 11)
                .mapToObj(index -> validCitation()).toList();
        assertTrue(validator.validate(proposal(eleven), candidateId, schemeId, Set.of(snapshotId))
                .stream().anyMatch(issue -> issue.code().equals("TOO_MANY_CITATIONS")));
    }

    @Test
    void refusesIncorrectEvidenceHash() {
        EvidenceCitation spoof = new EvidenceCitation(snapshotId, "Official fixture",
                "https://india.gov.in/fixture", "sha256:wrong", "Income requirement is 300000.");
        assertTrue(validator.validate(proposal(List.of(spoof)), candidateId, schemeId, Set.of(snapshotId))
                .stream().anyMatch(issue -> issue.code().equals("EVIDENCE_HASH_MISMATCH")));
    }

    private PolicyInvestigationResult proposal(List<EvidenceCitation> citations) {
        return new PolicyInvestigationResult(UUID.randomUUID(), AgentRunStatus.COMPLETED, candidateId, schemeId,
                List.of(snapshotId), null, "Synthetic policy proposal",
                new BooleanMatchRule("student", "Student status", FactKey.IS_STUDENT, true),
                citations, List.of(), List.of(), true, "Human verification required", true);
    }

    private EvidenceCitation validCitation() {
        return new EvidenceCitation(snapshotId, "Official fixture", "https://india.gov.in/fixture",
                "sha256:known", "Income requirement is 300000.");
    }
}
