package com.jansetu.backend.source.api;

import com.jansetu.backend.source.SourceSnapshot;

import java.time.Instant;
import java.util.UUID;

public record SourceSnapshotResponse(
        UUID id,
        UUID sourceId,
        String sourceName,
        String sourceUrl,
        String contentType,
        int httpStatus,
        long contentLength,
        String contentHash,
        String contentExcerpt,
        Instant capturedAt
) {
    static SourceSnapshotResponse from(SourceSnapshot snapshot) {
        return new SourceSnapshotResponse(snapshot.getId(), snapshot.getSource().getId(), snapshot.getSourceNameAtCapture(),
                snapshot.getSourceUrlAtCapture(), snapshot.getContentType(), snapshot.getHttpStatus(), snapshot.getContentLength(),
                snapshot.getContentHash(), snapshot.getContentExcerpt(), snapshot.getCapturedAt());
    }
}
