package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceVersionTest {
    private JwtService service() {
        JwtService jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secretKey",
                "test-only-secret-for-jansetu-security-tests-minimum-32-characters");
        ReflectionTestUtils.setField(jwt, "jwtExpiration", 3_600_000L);
        jwt.verifyConfiguration();
        return jwt;
    }

    @Test
    void acceptsCurrentTokenButRejectsItAfterPasswordRotation() {
        JwtService jwt = service();
        User user = new User();
        user.setEmail("citizen@jansetu.test");
        user.setEnabled(true);
        user.setTokenVersion(0);
        String oldToken = jwt.generateToken(user.getEmail(), 0);
        assertTrue(jwt.isTokenValid(oldToken, user));
        assertEquals(user.getEmail(), jwt.extractUsername(oldToken));

        user.setTokenVersion(1);
        assertFalse(jwt.isTokenValid(oldToken, user));
        String newToken = jwt.generateToken(user.getEmail(), 1);
        assertTrue(jwt.isTokenValid(newToken, user));
    }

    @Test
    void deniesTokenForDifferentAccountAndDisabledUser() {
        JwtService jwt = service();
        User user = new User();
        user.setEmail("one@jansetu.test");
        String token = jwt.generateToken("two@jansetu.test", 0);
        assertFalse(jwt.isTokenValid(token, user));
        user.setEmail("two@jansetu.test");
        user.setEnabled(false);
        assertFalse(jwt.isTokenValid(token, user));
    }

    @Test
    void rejectsWeakSecretAndExcessiveLifetimeAtStartup() {
        JwtService jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secretKey", "weak");
        ReflectionTestUtils.setField(jwt, "jwtExpiration", 3_600_000L);
        assertThrows(IllegalStateException.class, jwt::verifyConfiguration);
        ReflectionTestUtils.setField(jwt, "secretKey",
                "test-only-secret-for-jansetu-security-tests-minimum-32-characters");
        ReflectionTestUtils.setField(jwt, "jwtExpiration", 604_800_001L);
        assertThrows(IllegalStateException.class, jwt::verifyConfiguration);
    }
}
