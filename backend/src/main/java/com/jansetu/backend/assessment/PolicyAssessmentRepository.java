package com.jansetu.backend.assessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jansetu.backend.profile.entity.CitizenProfile;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface PolicyAssessmentRepository extends JpaRepository<PolicyAssessment, UUID> {
    @EntityGraph(attributePaths = {"policyVersion", "policyVersion.scheme"})
    List<PolicyAssessment> findByProfileIdOrderByAssessedAtDesc(UUID profileId);

    @EntityGraph(attributePaths = {"profile", "policyVersion", "policyVersion.scheme"})
    java.util.Optional<PolicyAssessment> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"policyVersion", "policyVersion.scheme"})
    Optional<PolicyAssessment> findTopByProfileIdAndPolicyVersionSchemeIdOrderByAssessedAtDesc(
            UUID profileId, UUID schemeId);

    @Query("select distinct assessment.profile from PolicyAssessment assessment " +
            "where assessment.policyVersion.scheme.id = :schemeId")
    List<CitizenProfile> findDistinctProfilesAssessedForScheme(@Param("schemeId") UUID schemeId);
}
