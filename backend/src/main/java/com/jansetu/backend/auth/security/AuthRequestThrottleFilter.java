package com.jansetu.backend.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rejects excessive requests even when the request body fails JSON/Bean Validation.
 * Installed as a servlet filter; it doesn't rely on an authenticated SecurityContext.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public final class AuthRequestThrottleFilter extends OncePerRequestFilter {
    private final AuthenticationRateLimiter limiter;

    public AuthRequestThrottleFilter(AuthenticationRateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getServletPath();
        // Some servlet containers expose an empty servletPath before dispatch.
        if (path == null || path.isEmpty()) {
            String requestUri = request.getRequestURI();
            String context = request.getContextPath();
            path = requestUri.substring(context == null ? 0 : context.length());
        }
        return !("/api/v1/auth/login".equals(path) || "/api/v1/auth/register".equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            limiter.checkAddress(request);
        } catch (AuthenticationRateLimitException exception) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", Integer.toString(AuthenticationRateLimitException.RETRY_AFTER_SECONDS));
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many authentication requests. Try again later.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
