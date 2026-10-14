package com.jansetu.backend.privacy;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.profile.entity.*;
import com.jansetu.backend.profile.repository.CitizenProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CitizenDataExportIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired CitizenProfileRepository profiles;

    @BeforeEach
    void fixtures() {
        createCitizen("export-one@example.test", "CitizenOne");
        createCitizen("export-two@example.test", "CitizenTwo");
    }

    private void createCitizen(String email, String firstName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("non-password-test-fixture-hash");
        user.setEnabled(true);
        user = users.saveAndFlush(user);

        CitizenProfile profile = new CitizenProfile();
        profile.setUser(user);
        profile.setFirstName(firstName);
        profile.setLastName("Tester");
        profile.setDateOfBirth(LocalDate.of(1998, 5, 12));
        profile.setGender(Gender.FEMALE);
        profile.setState("India");
        profile.setDistrict("Sample District");
        profile.setPincode("560001");
        profile.setCategory(Category.GENERAL);
        profile.setAnnualFamilyIncome(new BigDecimal("250000"));
        profile.setMaritalStatus(MaritalStatus.SINGLE);
        profile.setPrimaryOccupation(Occupation.STUDENT);
        profile.setStudent(true);
        profile.setFarmer(false);
        profile.setHasDisability(false);
        profiles.saveAndFlush(profile);
    }

    @Test
    void anonymousExportDenied() throws Exception {
        mvc.perform(get("/api/v1/citizen/data-export")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="export-one@example.test", roles="CITIZEN")
    void exportIsAccountScopedAndExplicitlyNonCacheable() throws Exception {
        var alice = mvc.perform(get("/api/v1/citizen/data-export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store, max-age=0"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"jansetu-my-data.json\""))
                .andExpect(jsonPath("$.exportVersion").value("1"))
                .andExpect(jsonPath("$.profile.firstName").value("CitizenOne"))
                .andExpect(jsonPath("$.savedSchemeIds.length()").value(0))
                .andReturn().getResponse().getContentAsString();
        assertFalse(alice.contains("CitizenTwo"));
        assertFalse(alice.contains("non-password-test-fixture-hash"));

        mvc.perform(get("/api/v1/citizen/data-export")
                .with(user("export-two@example.test").roles("CITIZEN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.firstName").value("CitizenTwo"));
    }
}
