package com.jansetu.backend.auth.security;

/** Public, generic error: never reveal whether an email is registered. */
public final class AuthenticationRateLimitException extends RuntimeException {
    public static final int RETRY_AFTER_SECONDS = 900;

    public AuthenticationRateLimitException() {
        super("Too many authentication requests. Try again later.");
    }
}
