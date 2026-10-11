package com.jansetu.backend.policy.registry;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, UUID> {
    List<PolicyVersion> findBySchemeIdOrderByVersionNumberDesc(UUID schemeId);

    Optional<PolicyVersion> findTopBySchemeIdOrderByVersionNumberDesc(UUID schemeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select policy from PolicyVersion policy where policy.scheme.id = :schemeId "
            + "and policy.status = com.jansetu.backend.policy.registry.PolicyVersionStatus.PUBLISHED "
            + "and policy.effectiveTo is null")
    Optional<PolicyVersion> lockCurrentPublishedVersion(@Param("schemeId") UUID schemeId);
}
