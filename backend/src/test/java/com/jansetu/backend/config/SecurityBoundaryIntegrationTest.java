package com.jansetu.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Guard against future additions to /auth accidentally becoming anonymously available,
 * and ensure bearer-only CORS cannot accidentally switch on credentialed requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityBoundaryIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void unknownAuthEndpointIsNotAutomaticallyPublic() throws Exception {
        mvc.perform(get("/api/v1/auth/diagnostics"))
                .andExpect(status().isForbidden());
    }

    @Test
    void disallowedOriginCannotUseCredentialedPreflight() throws Exception {
        mvc.perform(options("/api/v1/schemes")
                    .header("Origin", "https://untrusted.example")
                    .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowedOriginIsNotAuthorizedForCookieCredentials() throws Exception {
        mvc.perform(options("/api/v1/schemes")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void apiResponsesSetAntiClickjackingAndContentSniffingHeaders() throws Exception {
        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}
