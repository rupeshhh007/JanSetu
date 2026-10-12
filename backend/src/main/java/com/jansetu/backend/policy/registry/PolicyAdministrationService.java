package com.jansetu.backend.policy.registry;

import com.jansetu.backend.assessment.PolicyAssessmentService;
import com.jansetu.backend.audit.AdminAuditAction;
import com.jansetu.backend.audit.AdminAuditService;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.notification.CitizenPolicyNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class PolicyAdministrationService {
    private final PolicyVersionService policies;
    private final PolicyAssessmentService assessments;
    private final CitizenPolicyNotificationService notifications;
    private final AdminAuditService audit;

    public PolicyAdministrationService(PolicyVersionService policies, PolicyAssessmentService assessments,
                                       CitizenPolicyNotificationService notifications, AdminAuditService audit) {
        this.policies = policies;
        this.assessments = assessments;
        this.notifications = notifications;
        this.audit = audit;
    }

    @Transactional
    public PolicyVersion submitForReview(UUID schemeId, UUID policyVersionId, String reason, User administrator) {
        String reviewedReason = requireAdministratorAndReason(administrator, reason);
        PolicyVersion version = policies.submitForReview(schemeId, policyVersionId);
        record(administrator, AdminAuditAction.POLICY_SUBMITTED_FOR_REVIEW, version, reviewedReason, Map.of());
        return version;
    }

    @Transactional
    public PolicyVersion approve(UUID schemeId, UUID policyVersionId, String reason, User administrator) {
        String reviewedReason = requireAdministratorAndReason(administrator, reason);
        PolicyVersion version = policies.approve(schemeId, policyVersionId);
        record(administrator, AdminAuditAction.POLICY_APPROVED, version, reviewedReason, Map.of());
        return version;
    }

    @Transactional
    public PolicyVersion publish(UUID schemeId, UUID policyVersionId, String reason, User administrator) {
        String reviewedReason = requireAdministratorAndReason(administrator, reason);
        PolicyVersion version = policies.publish(schemeId, policyVersionId);
        var reassessments = assessments.reassessAll(version);
        reassessments.forEach(notifications::record);
        record(administrator, AdminAuditAction.POLICY_PUBLISHED, version, reviewedReason,
                Map.of("reassessedCitizens", Integer.toString(reassessments.size())));
        return version;
    }

    private void record(User administrator, AdminAuditAction action, PolicyVersion version, String reason,
                        Map<String, String> extraMetadata) {
        java.util.LinkedHashMap<String, String> metadata = new java.util.LinkedHashMap<>(extraMetadata);
        metadata.put("schemeId", version.getScheme().getId().toString());
        metadata.put("versionNumber", Integer.toString(version.getVersionNumber()));
        audit.record(administrator, action, "POLICY_VERSION", version.getId(), reason, metadata);
    }

    private String requireAdministratorAndReason(User administrator, String reason) {
        if (administrator == null || administrator.getId() == null || administrator.getRole() != Role.ADMIN) {
            throw new PolicyLifecycleException("An authenticated administrator is required.");
        }
        if (reason == null || reason.isBlank()) {
            throw new PolicyLifecycleException("An administrative reason is required.");
        }
        return reason.trim();
    }
}
