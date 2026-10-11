package com.jansetu.backend.assessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;

public interface PolicyAssessmentRepository extends JpaRepository<PolicyAssessment, UUID> {
    @EntityGraph(attributePaths = {"policyVersion", "policyVersion.scheme"})
    List<PolicyAssessment> findByProfileIdOrderByAssessedAtDesc(UUID profileId);

    @EntityGraph(attributePaths = {"profile", "policyVersion", "policyVersion.scheme"})
    java.util.Optional<PolicyAssessment> findWithDetailsById(UUID id);
}
