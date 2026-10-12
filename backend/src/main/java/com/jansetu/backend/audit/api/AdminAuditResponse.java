package com.jansetu.backend.audit.api;

import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditRecord;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AdminAuditResponse(UUID id, String actorEmail, AdminAuditAction action, String entityType,
                                 UUID entityId, String decisionReason, Map<String, String> metadata, Instant recordedAt) {
    static AdminAuditResponse from(AdminAuditRecord record) {
        return new AdminAuditResponse(record.getId(), record.getActor().getEmail(), record.getAction(), record.getEntityType(),
                record.getEntityId(), record.getDecisionReason(), record.getMetadata(), record.getRecordedAt());
    }
}
