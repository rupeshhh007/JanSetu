package com.jansetu.backend.profile.fact;

import com.jansetu.backend.policy.domain.FactKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CitizenProfileFactRepository extends JpaRepository<CitizenProfileFact, UUID> {
    List<CitizenProfileFact> findByProfileIdOrderByFactKeyAsc(UUID profileId);

    Optional<CitizenProfileFact> findByProfileIdAndFactKey(UUID profileId, FactKey factKey);
}
