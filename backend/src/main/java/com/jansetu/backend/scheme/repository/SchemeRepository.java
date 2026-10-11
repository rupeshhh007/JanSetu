package com.jansetu.backend.scheme.repository;

import com.jansetu.backend.scheme.entity.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import com.jansetu.backend.scheme.entity.SchemeStatus;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, UUID> {
    List<Scheme> findByStatusOrderByPublishedAtDesc(SchemeStatus status);
}
