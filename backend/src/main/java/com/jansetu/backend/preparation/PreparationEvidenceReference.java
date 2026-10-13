package com.jansetu.backend.preparation;

import java.util.UUID;

public record PreparationEvidenceReference(
        UUID snapshotId,
        String sourceTitle,
        String sourceUrl,
        String contentHash,
        String excerpt
) { }
