package com.jansetu.backend.preparation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationPreparationTaskRepository extends JpaRepository<ApplicationPreparationTask, UUID> {
    List<ApplicationPreparationTask> findBySessionIdOrderByTaskTypeAscTitleAsc(UUID sessionId);
    Optional<ApplicationPreparationTask> findByIdAndSessionId(UUID id, UUID sessionId);
    void deleteBySessionId(UUID sessionId);
}
