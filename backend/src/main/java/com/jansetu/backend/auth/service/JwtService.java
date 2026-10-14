package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Signed, short-lived JWTs that become invalid when the account token version
 * changes. No client-supplied roles are trusted; role information comes from DB.
 */
@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    void verifyConfiguration() {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET_KEY must contain at least 32 UTF-8 bytes");
        }
        if (jwtExpiration < 60_000L || jwtExpiration > 604_800_000L) {
            throw new IllegalStateException("JWT_EXPIRATION must be between 1 minute and 7 days");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, int tokenVersion) {
        if (tokenVersion < 0) {
            throw new IllegalArgumentException("Invalid account session version");
        }
        long issuedAt = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim("tv", tokenVersion)
                .issuedAt(new Date(issuedAt))
                .expiration(new Date(issuedAt + jwtExpiration))
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(signingKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    /** Verify both signature/expiry and current server-stored account version. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (!(userDetails instanceof User user) || !user.isEnabled()) {
            return false;
        }
        Claims claims = Jwts.parser().verifyWith(signingKey()).build()
                .parseSignedClaims(token).getPayload();
        Integer tokenVersion = claims.get("tv", Integer.class);
        Date expiration = claims.getExpiration();
        return user.getUsername().equals(claims.getSubject())
                && tokenVersion != null
                && tokenVersion == user.getTokenVersion()
                && expiration != null
                && expiration.after(new Date());
    }
}
