package com.jansetu.backend.policy.registry;

import java.util.UUID;

/** Immutable evidence reference captured with a policy version. */
public record PolicySourceReference(
        UUID snapshotId,
        String sourceUrl,
        String sourceTitle,
        String contentHash,
        String excerpt
) {
    /** Kept only to deserialize pre-registry policy drafts during the migration window. */
    public PolicySourceReference(String sourceUrl, String sourceTitle, String contentHash, String excerpt) {
        this(null, sourceUrl, sourceTitle, contentHash, excerpt);
    }
}
