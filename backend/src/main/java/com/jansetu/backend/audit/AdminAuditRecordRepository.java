package com.jansetu.backend.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;

public interface AdminAuditRecordRepository extends JpaRepository<AdminAuditRecord, UUID> {
    @EntityGraph(attributePaths = "actor")
    List<AdminAuditRecord> findTop100ByOrderByRecordedAtDesc();

    @EntityGraph(attributePaths = "actor")
    List<AdminAuditRecord> findByEntityTypeAndEntityIdOrderByRecordedAtDesc(String entityType, UUID entityId);
}
