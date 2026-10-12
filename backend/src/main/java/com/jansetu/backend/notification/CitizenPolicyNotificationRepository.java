package com.jansetu.backend.notification;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CitizenPolicyNotificationRepository extends JpaRepository<CitizenPolicyNotification, UUID> {
    @EntityGraph(attributePaths = {"scheme", "policyVersion"})
    List<CitizenPolicyNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
