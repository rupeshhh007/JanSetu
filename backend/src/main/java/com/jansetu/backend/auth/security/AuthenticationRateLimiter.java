package com.jansetu.backend.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * Bounded, single-instance abuse guard for the public authentication endpoints.
 *
 * This guard intentionally uses servlet remoteAddr rather than trusting an attacker-
 * supplied X-Forwarded-For header. It protects against simple abuse, but production
 * horizontal scaling must move rate-limit counters to a shared gateway or datastore.
 */
@Component
public final class AuthenticationRateLimiter {
    static final long WINDOW_MILLIS = Duration.ofMinutes(15).toMillis();
    private static final int MAX_TRACKED_KEYS = 40_000;
    private static final int MAX_REQUESTS_PER_ADDRESS = 150;
    private static final int MAX_LOGIN_ATTEMPTS_PER_IDENTITY = 12;
    private static final int MAX_REGISTRATIONS_PER_IDENTITY = 4;

    private final ConcurrentHashMap<String, Window> counters = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public AuthenticationRateLimiter() {
        this(System::currentTimeMillis);
    }

    AuthenticationRateLimiter(LongSupplier clock) {
        this.clock = clock;
    }

    /** Always called before parsing JSON in the auth controller by the servlet guard. */
    public void checkAddress(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        check("address:" + digest(remote == null ? "unknown" : remote), MAX_REQUESTS_PER_ADDRESS);
    }

    /** Called after Bean Validation, before any user lookup or password verification. */
    public void checkIdentity(String email, boolean registration) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        check((registration ? "register:" : "login:") + digest(normalized),
                registration ? MAX_REGISTRATIONS_PER_IDENTITY : MAX_LOGIN_ATTEMPTS_PER_IDENTITY);
    }

    private void check(String key, int allowance) {
        long now = clock.getAsLong();
        if (counters.size() >= MAX_TRACKED_KEYS && !counters.containsKey(key)) {
            counters.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= WINDOW_MILLIS);
            // Deny new identities rather than allocate unbounded JVM memory under attack.
            if (counters.size() >= MAX_TRACKED_KEYS) {
                throw new AuthenticationRateLimitException();
            }
        }
        AtomicBoolean rejected = new AtomicBoolean();
        counters.compute(key, (ignored, previous) -> {
            if (previous == null || now - previous.startedAt() >= WINDOW_MILLIS) {
                return new Window(now, 1);
            }
            if (previous.count() >= allowance) {
                rejected.set(true);
                return previous;
            }
            return new Window(previous.startedAt(), previous.count() + 1);
        });
        if (rejected.get()) {
            throw new AuthenticationRateLimitException();
        }
    }

    private static String digest(String input) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required for rate limiting", impossible);
        }
    }

    private record Window(long startedAt, int count) {}
}
