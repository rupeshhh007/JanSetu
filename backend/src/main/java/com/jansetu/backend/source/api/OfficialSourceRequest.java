package com.jansetu.backend.source.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OfficialSourceRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 160) String authority,
        @NotBlank @Size(max = 2048) String canonicalUrl
) { }
