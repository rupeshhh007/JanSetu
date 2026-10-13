package com.jansetu.backend.agent.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyAgentRunRepository extends JpaRepository<PolicyAgentRun, UUID> {
    @EntityGraph(attributePaths = {"sourceCandidate", "scheme", "startedBy"})
    Optional<PolicyAgentRun> findByStartedByIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @EntityGraph(attributePaths = {"sourceCandidate", "scheme", "startedBy"})
    @Query("select run from PolicyAgentRun run where run.id = :id")
    Optional<PolicyAgentRun> findDetailedById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"sourceCandidate", "sourceCandidate.previousSnapshot", "sourceCandidate.newSnapshot", "scheme", "startedBy", "reviewedBy"})
    @Query("select run from PolicyAgentRun run where run.id = :id")
    Optional<PolicyAgentRun> lockDetailedById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"sourceCandidate", "scheme", "startedBy"})
    List<PolicyAgentRun> findAllByOrderByCreatedAtDesc();
}
