package com.jansetu.backend.source.api;

import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.OfficialSourceStatus;

import java.time.Instant;
import java.util.UUID;

public record OfficialSourceResponse(
        UUID id,
        String name,
        String authority,
        String canonicalUrl,
        String normalizedHost,
        OfficialSourceStatus status,
        Instant lastSnapshotAt,
        Instant createdAt
) {
    static OfficialSourceResponse from(OfficialSource source) {
        return new OfficialSourceResponse(source.getId(), source.getName(), source.getAuthority(), source.getCanonicalUrl(),
                source.getNormalizedHost(), source.getStatus(), source.getLastSnapshotAt(), source.getCreatedAt());
    }
}
