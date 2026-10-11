package com.jansetu.backend.ai.dto;

import java.util.List;

/**
 * Small, deterministic UI payload.  The client may ignore it and render the
 * markdown response, but it must never need to parse model prose to find an ID.
 */
public record ChatCard(
        String type,
        String title,
        String description,
        String schemeId,
        List<String> highlights,
        String actionLabel,
        String actionHref
) { }
