package com.jansetu.backend.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationRateLimiterTest {
    @Test
    void blocksThirteenthLoginAttemptForSameIdentity() {
        AuthenticationRateLimiter limiter = new AuthenticationRateLimiter(() -> 1000L);
        for (int i = 0; i < 12; i++) {
            limiter.checkIdentity("  CITIZEN@EXAMPLE.COM  ", false);
        }
        assertThrows(AuthenticationRateLimitException.class,
                () -> limiter.checkIdentity("citizen@example.com", false));
    }

    @Test
    void doesNotShareIdentityBudgetsAcrossCitizens() {
        AuthenticationRateLimiter limiter = new AuthenticationRateLimiter(() -> 1000L);
        for (int i = 0; i < 12; i++) limiter.checkIdentity("a@example.com", false);
        limiter.checkIdentity("b@example.com", false);
        assertThrows(AuthenticationRateLimitException.class,
                () -> limiter.checkIdentity("a@example.com", false));
    }

    @Test
    void blocksFifthRegistrationForSameIdentity() {
        AuthenticationRateLimiter limiter = new AuthenticationRateLimiter(() -> 1000L);
        for (int i = 0; i < 4; i++) limiter.checkIdentity("person@example.com", true);
        assertThrows(AuthenticationRateLimitException.class,
                () -> limiter.checkIdentity("person@example.com", true));
    }

    @Test
    void expiredWindowAllowsNewAuthenticationAttempts() {
        AtomicLong now = new AtomicLong(5000L);
        AuthenticationRateLimiter limiter = new AuthenticationRateLimiter(now::get);
        for (int i = 0; i < 12; i++) limiter.checkIdentity("user@example.com", false);
        assertThrows(AuthenticationRateLimitException.class,
                () -> limiter.checkIdentity("user@example.com", false));
        now.addAndGet(AuthenticationRateLimiter.WINDOW_MILLIS);
        assertDoesNotThrow(() -> limiter.checkIdentity("user@example.com", false));
    }

    @Test
    void limitsUnknownOrSharedRemoteAddressWithoutForwardedHeaderTrust() {
        AuthenticationRateLimiter limiter = new AuthenticationRateLimiter(() -> 500L);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("198.51.100.50");
        request.addHeader("X-Forwarded-For", "127.0.0.1");
        for (int i = 0; i < 150; i++) limiter.checkAddress(request);
        assertThrows(AuthenticationRateLimitException.class,
                () -> limiter.checkAddress(request));
    }
}
