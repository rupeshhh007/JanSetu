package com.jansetu.backend.source;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/** Uses a bounded, redirect-free GET after SourceUrlPolicy approves the network destination. */
@Component
class JdkSourceFetcher implements SourceFetcher {
    private static final Set<String> ACCEPTED_CONTENT_TYPES = Set.of("text/html", "text/plain", "application/pdf");

    private final SourceUrlPolicy sourceUrlPolicy;
    private final HttpClient httpClient;
    private final int maxContentBytes;
    private final Duration requestTimeout;

    JdkSourceFetcher(SourceUrlPolicy sourceUrlPolicy,
                     @Value("${jansetu.sources.max-content-bytes:524288}") int maxContentBytes,
                     @Value("${jansetu.sources.request-timeout-seconds:10}") int timeoutSeconds) {
        if (maxContentBytes < 1024 || maxContentBytes > 5_242_880) {
            throw new IllegalArgumentException("jansetu.sources.max-content-bytes must be between 1024 and 5242880.");
        }
        if (timeoutSeconds < 1 || timeoutSeconds > 30) {
            throw new IllegalArgumentException("jansetu.sources.request-timeout-seconds must be between 1 and 30.");
        }
        this.sourceUrlPolicy = sourceUrlPolicy;
        this.maxContentBytes = maxContentBytes;
        this.requestTimeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.requestTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public RetrievedSource fetch(URI sourceUrl) {
        sourceUrlPolicy.validateForRetrieval(sourceUrl);
        HttpRequest request = HttpRequest.newBuilder(sourceUrl)
                .GET()
                .timeout(requestTimeout)
                .header("Accept", "text/html, text/plain, application/pdf")
                .header("User-Agent", "JanSetuSourceVerifier/1.0")
                .build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SourceIntakeException("The official source did not return a successful response; redirects are not followed.");
            }
            String contentType = mediaType(response.headers().firstValue("Content-Type").orElse(""));
            if (!ACCEPTED_CONTENT_TYPES.contains(contentType)) {
                throw new SourceIntakeException("Only HTML, plain-text, and PDF official source documents may be captured.");
            }
            long advertisedLength = response.headers().firstValueAsLong("Content-Length").orElse(-1);
            if (advertisedLength > maxContentBytes) {
                throw new SourceIntakeException("The official source document exceeds the configured capture limit.");
            }
            try (InputStream body = response.body()) {
                byte[] content = body.readNBytes(maxContentBytes + 1);
                if (content.length > maxContentBytes) {
                    throw new SourceIntakeException("The official source document exceeds the configured capture limit.");
                }
                return new RetrievedSource(sourceUrl, response.statusCode(), contentType, content);
            }
        } catch (IOException exception) {
            throw new SourceIntakeException("The official source could not be retrieved safely.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SourceIntakeException("Official source retrieval was interrupted.");
        }
    }

    private String mediaType(String contentType) {
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }
}
