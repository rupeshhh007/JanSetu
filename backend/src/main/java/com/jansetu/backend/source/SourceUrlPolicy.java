package com.jansetu.backend.source;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Enforces the allowlist and network boundary before any server-side retrieval. */
@Component
public class SourceUrlPolicy {
    private final Set<String> allowedHosts;
    private final HostResolver hostResolver;

    public SourceUrlPolicy(
            @Value("${jansetu.sources.allowed-hosts:mahadbt.maharashtra.gov.in,pmkisan.gov.in,india.gov.in}") String configuredHosts,
            HostResolver hostResolver
    ) {
        this.allowedHosts = Arrays.stream(configuredHosts.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
        this.hostResolver = hostResolver;
        if (allowedHosts.isEmpty()) {
            throw new IllegalStateException("At least one official source host must be allowlisted.");
        }
    }

    public URI canonicalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new SourceIntakeException("An official source URL is required.");
        }
        final URI uri;
        try {
            uri = URI.create(rawUrl.trim()).normalize();
        } catch (IllegalArgumentException exception) {
            throw new SourceIntakeException("Official source URLs must be valid HTTP or HTTPS URLs.");
        }
        if (!uri.isAbsolute() || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null
                || (!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme()))) {
            throw new SourceIntakeException("Official source URLs must be absolute HTTP or HTTPS URLs without credentials or fragments.");
        }
        if (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
            throw new SourceIntakeException("Official source URLs must use the standard HTTP or HTTPS ports.");
        }
        String host = normalizedHost(uri);
        if (!allowedHosts.contains(host)) {
            throw new SourceIntakeException("The source host is not on the configured official-source allowlist.");
        }
        return uri;
    }

    public void validateForRetrieval(URI url) {
        String host = normalizedHost(url);
        if (!allowedHosts.contains(host)) {
            throw new SourceIntakeException("The source host is not on the configured official-source allowlist.");
        }
        try {
            List<InetAddress> resolvedAddresses = hostResolver.resolve(host);
            if (resolvedAddresses.isEmpty() || resolvedAddresses.stream().anyMatch(this::isPrivateOrLocal)) {
                throw new SourceIntakeException("Official source hosts must not resolve to private or local network addresses.");
            }
        } catch (UnknownHostException exception) {
            throw new SourceIntakeException("The official source host could not be resolved.");
        }
    }

    public String normalizedHost(URI uri) {
        if (uri == null || uri.getHost() == null) {
            throw new SourceIntakeException("Official source URLs must include a host.");
        }
        return uri.getHost().toLowerCase(Locale.ROOT);
    }

    private boolean isPrivateOrLocal(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            return first == 10 || first == 127 || first == 0
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && second == 168)
                    || (first == 169 && second == 254)
                    || first >= 224;
        }
        return address instanceof Inet6Address && (bytes[0] & 0xfe) == 0xfc;
    }
}
