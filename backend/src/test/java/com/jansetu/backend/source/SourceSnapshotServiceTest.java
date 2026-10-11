package com.jansetu.backend.source;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceSnapshotServiceTest {
    @Test
    void capturesExactBytesWithAStableHashAndSafeHtmlExcerpt() {
        OfficialSourceRepository sources = mock(OfficialSourceRepository.class);
        SourceSnapshotRepository snapshots = mock(SourceSnapshotRepository.class);
        SourceUrlPolicy urls = mock(SourceUrlPolicy.class);
        SourceFetcher fetcher = mock(SourceFetcher.class);
        Clock clock = Clock.fixed(Instant.parse("2026-10-11T12:00:00Z"), ZoneOffset.UTC);
        SourceSnapshotService service = new SourceSnapshotService(sources, snapshots, urls, fetcher, clock);

        UUID sourceId = UUID.randomUUID();
        URI sourceUrl = URI.create("https://india.gov.in/welfare");
        OfficialSource source = new OfficialSource();
        source.setName("India portal");
        source.setAuthority("National Informatics Centre");
        source.setCanonicalUrl(sourceUrl.toString());
        source.setNormalizedHost("india.gov.in");
        byte[] content = "<main>Benefit <script>alert('ignored')</script>details</main>".getBytes(StandardCharsets.UTF_8);

        when(sources.findById(sourceId)).thenReturn(java.util.Optional.of(source));
        when(fetcher.fetch(eq(sourceUrl))).thenReturn(new RetrievedSource(sourceUrl, 200, "text/html", content));
        when(snapshots.save(any(SourceSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SourceSnapshot snapshot = service.capture(sourceId);

        assertEquals("sha256:beaef5b6f398330890126cb9ef910664a189917068f80f202124bddd5c965b6a", snapshot.getContentHash());
        assertEquals("Benefit details", snapshot.getContentExcerpt());
        assertArrayEquals(content, snapshot.getContentBytes());
        assertEquals(Instant.parse("2026-10-11T12:00:00Z"), snapshot.getCapturedAt());
        assertEquals(snapshot.getCapturedAt(), source.getLastSnapshotAt());
    }
}
