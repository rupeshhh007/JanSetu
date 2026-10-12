package com.jansetu.backend.source.change;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SourceChangeCandidateRepository extends JpaRepository<SourceChangeCandidate, UUID> {
    List<SourceChangeCandidate> findByStatusOrderByCreatedAtDesc(SourceChangeCandidateStatus status);
}
