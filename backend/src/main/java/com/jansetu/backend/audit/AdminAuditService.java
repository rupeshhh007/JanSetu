package com.jansetu.backend.audit;

import com.jansetu.backend.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class AdminAuditService {
    private final AdminAuditRecordRepository records;
    private final Clock clock;

    @Autowired
    public AdminAuditService(AdminAuditRecordRepository records) { this(records, Clock.systemUTC()); }
    AdminAuditService(AdminAuditRecordRepository records, Clock clock) { this.records = records; this.clock = clock; }

    /** Must be invoked inside the decision transaction; failures roll back that decision. */
    public AdminAuditRecord record(User actor, AdminAuditAction action, String entityType, UUID entityId,
                                   String reason, Map<String, String> safeMetadata) {
        AdminAuditRecord record = new AdminAuditRecord();
        record.setActor(actor);
        record.setAction(action);
        record.setEntityType(entityType);
        record.setEntityId(entityId);
        record.setDecisionReason(reason);
        record.setMetadata(safeMetadata);
        record.setRecordedAt(Instant.now(clock));
        return records.save(record);
    }

    public List<AdminAuditRecord> recent() { return records.findTop100ByOrderByRecordedAtDesc(); }
}
