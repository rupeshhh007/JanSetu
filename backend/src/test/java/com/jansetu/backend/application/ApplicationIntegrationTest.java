package com.jansetu.backend.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.application.dto.ApplicationRequest;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.profile.entity.*;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CitizenProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        String testUsername = "testuser";

        // 1. Ensure User exists
        User user = userRepository.findByEmail(testUsername)
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(testUsername);
                    u.setPasswordHash(passwordEncoder.encode("password123"));
                    u.setRole(com.jansetu.backend.auth.entity.Role.CITIZEN);
                    u.setEnabled(true);
                    return userRepository.saveAndFlush(u);
                });

        // 2. Refresh user to ensure it is fully managed by Hibernate
        user = userRepository.findById(user.getId()).orElse(user);

        // 3. Create Profile only if it absolutely doesn't exist
        if (profileRepository.findByUser(user).isEmpty()) {
            CitizenProfile profile = new CitizenProfile();
            profile.setUser(user);
            profile.setFirstName("Test");
            profile.setLastName("User");
            profile.setDateOfBirth(LocalDate.of(1990, 1, 1));
            profile.setGender(Gender.MALE);
            profile.setState("Karnataka");
            profile.setDistrict("Bangalore");
            profile.setPincode("560000");
            profile.setCategory(Category.GENERAL);
            profile.setAnnualFamilyIncome(new BigDecimal("100000.00"));
            profile.setMaritalStatus(MaritalStatus.SINGLE);
            profile.setPrimaryOccupation(Occupation.STUDENT);
            profile.setStudent(true);
            profile.setFarmer(false);
            profile.setHasDisability(false);

            profileRepository.saveAndFlush(profile);
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    void submitApplication_ValidRequest_Returns200() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setSchemeId(UUID.fromString("5815c235-d20c-45fc-89cf-c2d9e847b8e4"));

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").exists())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.schemeName").value("Post Matric Scholarship"));
    }
}