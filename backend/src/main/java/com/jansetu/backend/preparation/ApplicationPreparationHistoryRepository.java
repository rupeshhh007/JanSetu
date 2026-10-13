package com.jansetu.backend.preparation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationPreparationHistoryRepository extends JpaRepository<ApplicationPreparationHistory, UUID> {
    @EntityGraph(attributePaths = "actor")
    List<ApplicationPreparationHistory> findBySessionIdOrderByRecordedAtDesc(UUID sessionId);
}
