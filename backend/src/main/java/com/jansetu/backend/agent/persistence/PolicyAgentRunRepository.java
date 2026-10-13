package com.jansetu.backend.agent.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyAgentRunRepository extends JpaRepository<PolicyAgentRun, UUID> {
    Optional<PolicyAgentRun> findByStartedByIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @EntityGraph(attributePaths = {"sourceCandidate", "scheme", "startedBy"})
    List<PolicyAgentRun> findAllByOrderByCreatedAtDesc();
}
