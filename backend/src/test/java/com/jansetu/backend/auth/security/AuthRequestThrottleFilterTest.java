package com.jansetu.backend.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthRequestThrottleFilterTest {
    @Test
    void rejectsMalformedLoginFloodBeforeParsingJson() throws Exception {
        AuthenticationRateLimiter limiter = new AuthenticationRateLimiter(() -> 10_000L);
        AuthRequestThrottleFilter filter = new AuthRequestThrottleFilter(limiter);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("203.0.113.18");
        request.setServletPath("/api/v1/auth/login");
        for (int i = 0; i < 150; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request, blocked, chain);
        assertEquals(429, blocked.getStatus());
        assertEquals("900", blocked.getHeader("Retry-After"));
        assertEquals("no-store", blocked.getHeader("Cache-Control"));
        assertTrue(blocked.getContentAsString().contains("Too Many Requests"));
        verify(chain, times(150)).doFilter(any(), any());
    }

    @Test
    void doesNotThrottleUnrelatedEndpoints() throws Exception {
        AuthRequestThrottleFilter filter =
                new AuthRequestThrottleFilter(new AuthenticationRateLimiter(() -> 1L));
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/schemes"),
                new MockHttpServletResponse(), chain);
        verify(chain).doFilter(any(), any());
    }
}
