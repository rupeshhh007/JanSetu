package com.jansetu.backend.source;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OfficialSourceRepository extends JpaRepository<OfficialSource, UUID> {
    List<OfficialSource> findAllByOrderByNameAsc();
    boolean existsByCanonicalUrl(String canonicalUrl);
}
