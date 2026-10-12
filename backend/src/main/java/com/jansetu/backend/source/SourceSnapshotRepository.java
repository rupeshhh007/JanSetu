package com.jansetu.backend.source;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceSnapshotRepository extends JpaRepository<SourceSnapshot, UUID> {
    @EntityGraph(attributePaths = "source")
    List<SourceSnapshot> findBySourceIdOrderByCapturedAtDesc(UUID sourceId);

    @EntityGraph(attributePaths = "source")
    Optional<SourceSnapshot> findFirstBySourceIdOrderByCapturedAtDesc(UUID sourceId);

    @Override
    @EntityGraph(attributePaths = "source")
    Optional<SourceSnapshot> findById(UUID id);
}
