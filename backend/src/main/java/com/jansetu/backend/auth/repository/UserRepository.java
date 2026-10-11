package com.jansetu.backend.auth.repository;

import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByRole(Role role);

}