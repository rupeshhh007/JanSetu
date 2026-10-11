package com.jansetu.backend.source;

import java.net.URI;
import java.util.Arrays;

/** Bounded HTTP response accepted for evidence capture. */
public record RetrievedSource(URI sourceUrl, int statusCode, String contentType, byte[] content) {
    public RetrievedSource {
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
