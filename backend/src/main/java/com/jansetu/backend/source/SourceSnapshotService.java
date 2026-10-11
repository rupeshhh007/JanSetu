package com.jansetu.backend.source;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Registers approved authorities and records replayable, immutable evidence snapshots. */
@Service
public class SourceSnapshotService {
    private static final int EXCERPT_MAX_LENGTH = 2_000;

    private final OfficialSourceRepository sources;
    private final SourceSnapshotRepository snapshots;
    private final SourceUrlPolicy sourceUrlPolicy;
    private final SourceFetcher sourceFetcher;
    private final Clock clock;

    @Autowired
    public SourceSnapshotService(OfficialSourceRepository sources, SourceSnapshotRepository snapshots,
                                 SourceUrlPolicy sourceUrlPolicy, SourceFetcher sourceFetcher) {
        this(sources, snapshots, sourceUrlPolicy, sourceFetcher, Clock.systemUTC());
    }

    SourceSnapshotService(OfficialSourceRepository sources, SourceSnapshotRepository snapshots,
                          SourceUrlPolicy sourceUrlPolicy, SourceFetcher sourceFetcher, Clock clock) {
        this.sources = sources;
        this.snapshots = snapshots;
        this.sourceUrlPolicy = sourceUrlPolicy;
        this.sourceFetcher = sourceFetcher;
        this.clock = clock;
    }

    @Transactional
    public OfficialSource register(OfficialSourceCreateCommand command) {
        String name = required(command.name(), "An official source name is required.");
        String authority = required(command.authority(), "The responsible authority is required.");
        URI canonicalUrl = sourceUrlPolicy.canonicalize(command.canonicalUrl());
        if (sources.existsByCanonicalUrl(canonicalUrl.toASCIIString())) {
            throw new SourceIntakeException("That official source URL is already registered.");
        }
        OfficialSource source = new OfficialSource();
        source.setName(name);
        source.setAuthority(authority);
        source.setCanonicalUrl(canonicalUrl.toASCIIString());
        source.setNormalizedHost(sourceUrlPolicy.normalizedHost(canonicalUrl));
        return sources.save(source);
    }

    @Transactional(readOnly = true)
    public List<OfficialSource> listSources() {
        return sources.findAllByOrderByNameAsc();
    }

    @Transactional
    public SourceSnapshot capture(UUID sourceId) {
        OfficialSource source = findSource(sourceId);
        if (source.getStatus() != OfficialSourceStatus.ACTIVE) {
            throw new SourceIntakeException("Disabled official sources cannot capture new evidence snapshots.");
        }
        URI sourceUrl = URI.create(source.getCanonicalUrl());
        RetrievedSource retrieved = sourceFetcher.fetch(sourceUrl);
        if (!sourceUrl.equals(retrieved.sourceUrl())) {
            throw new SourceIntakeException("The fetched source URL did not match the registered official source URL.");
        }

        Instant capturedAt = Instant.now(clock);
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSource(source);
        snapshot.setSourceNameAtCapture(source.getName());
        snapshot.setSourceUrlAtCapture(source.getCanonicalUrl());
        snapshot.setContentType(retrieved.contentType());
        snapshot.setHttpStatus(retrieved.statusCode());
        snapshot.setContentLength(retrieved.content().length);
        snapshot.setContentHash(contentHash(retrieved.content()));
        snapshot.setContentExcerpt(excerpt(retrieved.contentType(), retrieved.content()));
        snapshot.setContentBytes(retrieved.content());
        snapshot.setCapturedAt(capturedAt);
        source.setLastSnapshotAt(capturedAt);
        return snapshots.save(snapshot);
    }

    @Transactional(readOnly = true)
    public List<SourceSnapshot> history(UUID sourceId) {
        findSource(sourceId);
        return snapshots.findBySourceIdOrderByCapturedAtDesc(sourceId);
    }

    @Transactional
    public OfficialSource disable(UUID sourceId) {
        OfficialSource source = findSource(sourceId);
        source.setStatus(OfficialSourceStatus.DISABLED);
        return source;
    }

    private OfficialSource findSource(UUID id) {
        return sources.findById(id)
                .orElseThrow(() -> new OfficialSourceNotFoundException("Official source not found"));
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new SourceIntakeException(message);
        }
        return value.trim();
    }

    private String contentHash(byte[] content) {
        try {
            return "sha256:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available in the JVM", exception);
        }
    }

    private String excerpt(String contentType, byte[] content) {
        if ("application/pdf".equals(contentType)) {
            return "PDF evidence captured. Review the retained immutable source bytes in an administrative evidence workflow.";
        }
        String text = new String(content, StandardCharsets.UTF_8);
        if ("text/html".equals(contentType)) {
            text = text.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                    .replaceAll("(?is)<[^>]+>", " ")
                    .replace("&nbsp;", " ")
                    .replace("&amp;", "&");
        }
        text = text.replaceAll("\\s+", " ").trim();
        return text.length() <= EXCERPT_MAX_LENGTH ? text : text.substring(0, EXCERPT_MAX_LENGTH) + "…";
    }
}
