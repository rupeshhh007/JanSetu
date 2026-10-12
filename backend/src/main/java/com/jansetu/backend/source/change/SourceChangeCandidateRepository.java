package com.jansetu.backend.source.change;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;

public interface SourceChangeCandidateRepository extends JpaRepository<SourceChangeCandidate, UUID> {
    @EntityGraph(attributePaths = {"source", "previousSnapshot", "newSnapshot", "affectedScheme", "reviewedBy", "proposedPolicyVersion", "draftedBy"})
    List<SourceChangeCandidate> findByStatusOrderByCreatedAtDesc(SourceChangeCandidateStatus status);

    @EntityGraph(attributePaths = {"source", "previousSnapshot", "newSnapshot", "affectedScheme", "reviewedBy", "proposedPolicyVersion", "draftedBy"})
    List<SourceChangeCandidate> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"source", "previousSnapshot", "newSnapshot", "affectedScheme", "reviewedBy", "proposedPolicyVersion", "draftedBy"})
    @Query("select candidate from SourceChangeCandidate candidate where candidate.id = :id")
    java.util.Optional<SourceChangeCandidate> lockById(UUID id);
}
