package com.jansetu.backend.source;

import java.net.URI;

public interface SourceFetcher {
    RetrievedSource fetch(URI sourceUrl);
}
