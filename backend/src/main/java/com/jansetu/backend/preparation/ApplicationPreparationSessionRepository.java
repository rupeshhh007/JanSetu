package com.jansetu.backend.preparation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationPreparationSessionRepository extends JpaRepository<ApplicationPreparationSession, UUID> {
    @EntityGraph(attributePaths = {"scheme", "policyVersion"})
    List<ApplicationPreparationSession> findByProfileIdOrderByUpdatedAtDesc(UUID profileId);

    @EntityGraph(attributePaths = {"scheme", "policyVersion"})
    Optional<ApplicationPreparationSession> findByProfileIdAndSchemeId(UUID profileId, UUID schemeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"profile", "profile.user", "scheme", "policyVersion"})
    @Query("select session from ApplicationPreparationSession session where session.id = :id and session.profile.id = :profileId")
    Optional<ApplicationPreparationSession> lockOwned(@Param("id") UUID id, @Param("profileId") UUID profileId);
}
