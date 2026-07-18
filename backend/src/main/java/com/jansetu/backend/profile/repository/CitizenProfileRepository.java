package com.jansetu.backend.profile.repository;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.profile.entity.CitizenProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CitizenProfileRepository extends JpaRepository<CitizenProfile, UUID> {

    // Spring Data JPA magic: We just name the method correctly, and Spring writes the SQL!
    Optional<CitizenProfile> findByUser(User user);

}