package com.jansetu.backend.policy.registry;

/** Immutable evidence reference captured with a policy version. */
public record PolicySourceReference(
        String sourceUrl,
        String sourceTitle,
        String contentHash,
        String excerpt
) { }
