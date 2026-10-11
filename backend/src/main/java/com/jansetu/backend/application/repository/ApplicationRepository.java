package com.jansetu.backend.application.repository;

import com.jansetu.backend.application.entity.Application;
import com.jansetu.backend.application.enums.ApplicationStatus;
import com.jansetu.backend.profile.entity.CitizenProfile;
import com.jansetu.backend.scheme.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    // 1. Used to prevent duplicates.
    // We check if an application exists for this profile and scheme that is NOT REJECTED or WITHDRAWN
    boolean existsByCitizenProfileAndSchemeAndStatusIn(
            CitizenProfile profile,
            Scheme scheme,
            Collection<ApplicationStatus> statuses
    );

    // 2. Used for the GET /applications/my endpoint
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "scheme")
    List<Application> findByCitizenProfile(CitizenProfile profile);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "scheme")
    List<Application> findAllByOrderBySubmittedAtDesc();
}
