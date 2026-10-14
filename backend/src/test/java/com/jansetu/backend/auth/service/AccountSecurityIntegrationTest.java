package com.jansetu.backend.auth.service;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountSecurityIntegrationTest {
    static final String EMAIL = "password-rotation@jansetu.test";
    static final String OLD = "old-secure-password123!";
    static final String NEW = "new-secure-password456!";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtService jwt;

    @BeforeEach
    void user() {
        User account = new User();
        account.setEmail(EMAIL);
        account.setPasswordHash(passwords.encode(OLD));
        account.setEnabled(true);
        users.saveAndFlush(account);
    }

    @Test
    @WithMockUser(username=EMAIL, roles="CITIZEN")
    void successfulPasswordChangeRevokesEveryOldToken() throws Exception {
        User account = users.findByEmail(EMAIL).orElseThrow();
        String oldJwt = jwt.generateToken(EMAIL, account.getTokenVersion());
        mvc.perform(post("/api/v1/citizen/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + OLD + "\",\"newPassword\":\"" + NEW + "\"}"))
                .andExpect(status().isNoContent());
        account = users.findByEmail(EMAIL).orElseThrow();
        assertEquals(1, account.getTokenVersion());
        assertFalse(jwt.isTokenValid(oldJwt, account));
        assertTrue(passwords.matches(NEW, account.getPasswordHash()));
        assertFalse(passwords.matches(OLD, account.getPasswordHash()));
    }

    @Test
    @WithMockUser(username=EMAIL, roles="CITIZEN")
    void rejectsBadCurrentPasswordAndPasswordReuseWithoutRevocation() throws Exception {
        mvc.perform(post("/api/v1/citizen/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"wrong-secret\",\"newPassword\":\"" + NEW + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/citizen/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + OLD + "\",\"newPassword\":\"" + OLD + "\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, users.findByEmail(EMAIL).orElseThrow().getTokenVersion());
    }

    @Test
    @WithMockUser(username=EMAIL, roles="CITIZEN")
    void rejectsWeakNewPassword() throws Exception {
        mvc.perform(post("/api/v1/citizen/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + OLD + "\",\"newPassword\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCannotRotatePassword() throws Exception {
        mvc.perform(post("/api/v1/citizen/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"x\",\"newPassword\":\"a-long-new-password\"}"))
                .andExpect(status().isForbidden());
    }
}
