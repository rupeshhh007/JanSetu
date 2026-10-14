package com.jansetu.backend.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired ObjectMapper json;

    @Test
    void registrationCreatesOnlyCitizenWithBcryptHash() throws Exception {
        String email = "registration-unique@jansetu.test";
        String password = "A-Correct-Horse-12345";
        String payload = json.writeValueAsString(java.util.Map.of("email", email, "password", password));
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CITIZEN"));
        var user = users.findByEmail(email).orElseThrow();
        assertEquals(Role.CITIZEN, user.getRole());
        assertNotEquals(password, user.getPasswordHash());
        assertTrue(passwords.matches(password, user.getPasswordHash()));
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsWeakOrBcryptTruncatedSignupPasswords() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"weak@jansetu.test\",\"password\":\"abc123\"}"))
                .andExpect(status().isBadRequest());
        String hugeUnicodePassword = "🔒".repeat(30);
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("email", "unicode@jansetu.test", "password", hugeUnicodePassword))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownAndIncorrectPasswordsHaveSamePublicMessage() throws Exception {
        String known = "enumeration-known@jansetu.test";
        String raw = "Valid-Password-98765";
        users.saveAndFlush(newUser(known, passwords.encode(raw)));
        String knownPayload = json.writeValueAsString(java.util.Map.of("email", known, "password", "incorrect-password"));
        String unknownPayload = json.writeValueAsString(java.util.Map.of("email", "enumeration-unknown@jansetu.test", "password", "incorrect-password"));

        String knownError = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(knownPayload)).andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknownError = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(unknownPayload)).andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertEquals(json.readTree(knownError).get("message").asText(),
                json.readTree(unknownError).get("message").asText());
    }

    @Test
    void repeatedLoginAttemptsReturnRetryAfter() throws Exception {
        String payload = json.writeValueAsString(java.util.Map.of(
                "email", "rate-checked@jansetu.test", "password", "wrong-password"));
        for (int i = 0; i < 12; i++) {
            mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"));
    }

    private com.jansetu.backend.auth.entity.User newUser(String email, String hashedPassword) {
        var user = new com.jansetu.backend.auth.entity.User();
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setEnabled(true);
        return user;
    }
}
