package com.jansetu.backend.source.change;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

public interface SourceChangeCandidateRepository extends JpaRepository<SourceChangeCandidate, UUID> {
    List<SourceChangeCandidate> findByStatusOrderByCreatedAtDesc(SourceChangeCandidateStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select candidate from SourceChangeCandidate candidate where candidate.id = :id")
    java.util.Optional<SourceChangeCandidate> lockById(UUID id);
}
