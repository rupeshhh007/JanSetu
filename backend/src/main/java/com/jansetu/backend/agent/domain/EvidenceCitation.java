package com.jansetu.backend.agent.domain;

import java.util.UUID;

public record EvidenceCitation(
        UUID snapshotId,
        String sourceTitle,
        String sourceUrl,
        String contentHash,
        String exactSpan
) { }
