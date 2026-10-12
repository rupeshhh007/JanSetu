package com.jansetu.backend.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminAuditRecordRepository extends JpaRepository<AdminAuditRecord, UUID> {
    List<AdminAuditRecord> findTop100ByOrderByRecordedAtDesc();
    List<AdminAuditRecord> findByEntityTypeAndEntityIdOrderByRecordedAtDesc(String entityType, UUID entityId);
}
