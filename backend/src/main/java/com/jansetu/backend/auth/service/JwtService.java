package com.jansetu.backend.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    // 1. Spring pulls these values from your environment variables/properties!
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // 2. Convert the secret string into a cryptographic key
    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 3. Generate the actual VIP Badge
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email) // The "who" this badge belongs to
                .issuedAt(new Date(System.currentTimeMillis())) // The "when" it was printed
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // The "when" it expires
                .signWith(getSignInKey()) // The cryptographic signature
                .compact(); // Squishes it all into the final String token
    }
    // NEW: Extract the email (subject) from the token
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey()) // Uses our secret key to check the signature!
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // NEW: Check if the token is valid for the specific user
    public boolean isTokenValid(String token, String userEmail) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(userEmail) && !isTokenExpired(token));
    }

    // NEW: Helper method to check expiration
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiration.before(new Date());
    }
}