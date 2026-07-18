package com.jansetu.backend.scheme.repository;

import com.jansetu.backend.scheme.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, UUID> {
    // For now, we get all the standard methods (save, findById, findAll) for free!
    // We will add custom JSONB query methods here as soon as we build the search logic.
}