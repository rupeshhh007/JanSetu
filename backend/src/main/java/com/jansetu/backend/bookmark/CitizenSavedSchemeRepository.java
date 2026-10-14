package com.jansetu.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CitizenSavedSchemeRepository extends JpaRepository<CitizenSavedScheme, UUID> {
    List<CitizenSavedScheme> findByCitizenIdOrderByCreatedAtDesc(UUID citizenId);
    Optional<CitizenSavedScheme> findByCitizenIdAndSchemeId(UUID citizenId, UUID schemeId);
}
