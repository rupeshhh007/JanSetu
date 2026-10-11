package com.jansetu.backend.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.application.dto.ApplicationRequest;
import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
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
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApplicationIntegrationTest {
    @Autowired
    private SchemeRepository schemeRepository;
    private UUID schemeId;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

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
        // A test owns all of its fixture data; it must not rely on a developer's
        // seeded catalogue or an external/local PostgreSQL database.
        Scheme scheme = new Scheme();
        scheme.setName("Test student support");
        scheme.setDescription("A deterministic fixture for application submission.");
        scheme.setState("Karnataka");
        scheme.setDepartment("Test Department");
        EligibilityCriteria eligibilityCriteria = new EligibilityCriteria();
        eligibilityCriteria.setMaxFamilyIncome(new BigDecimal("500000.00"));
        scheme.setEligibilityCriteria(eligibilityCriteria);
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setPublishedAt(Instant.now());
        scheme.setBenefits(java.util.List.of("Test benefit"));
        scheme.setRequiredDocuments(java.util.List.of("Test identity document"));
        scheme = schemeRepository.saveAndFlush(scheme);

        schemeId = scheme.getId();
    }

    @Test
    @WithMockUser(username = "testuser")
    void submitApplication_ValidRequest_Returns200() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setSchemeId(schemeId);

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").exists())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.schemeName").isNotEmpty());

    }

    @Test
    @WithMockUser(username = "testuser", roles = "CITIZEN")
    void citizenCannotOpenAdminApplicationQueue() throws Exception {
        mockMvc.perform(get("/api/v1/admin/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aiChatRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void rejectsApplicationsToArchivedSchemes() throws Exception {
        Scheme scheme = schemeRepository.findById(schemeId).orElseThrow();
        scheme.setStatus(SchemeStatus.ARCHIVED);
        schemeRepository.saveAndFlush(scheme);
        submit().andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void rejectsApplicationsOutsideCitizenState() throws Exception {
        Scheme scheme = schemeRepository.findById(schemeId).orElseThrow();
        scheme.setState("Maharashtra");
        schemeRepository.saveAndFlush(scheme);
        submit().andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/schemes/" + schemeId + "/eligibility"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.outcome").value("NOT_ELIGIBLE"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void rejectsDuplicateActiveApplications() throws Exception {
        submit().andExpect(status().isOk());
        submit().andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testuser")
    void guideFollowUpUsesPublishedContextAndDeterministicEligibility() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("message", "What documents do I need?",
                                "contextSchemeIds", java.util.List.of(schemeId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].schemeId").value(schemeId.toString()))
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Test identity document")));
        mockMvc.perform(post("/api/v1/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("message", "Am I eligible for this?",
                                "contextSchemeIds", java.util.List.of(schemeId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Your recorded family income")));
    }

    @Test
    @WithMockUser(username = "testuser")
    void citizenWithoutProfileCanUseGuideAndSeeEmptyApplications() throws Exception {
        User user = userRepository.findByEmail("testuser").orElseThrow();
        profileRepository.delete(profileRepository.findByUser(user).orElseThrow());
        profileRepository.flush();
        mockMvc.perform(get("/api/v1/applications")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(post("/api/v1/ai/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Which schemes should I apply for?\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response").value(org.hamcrest.Matchers.containsString("Create your citizen profile")));
        mockMvc.perform(get("/api/v1/schemes/" + schemeId + "/eligibility"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.outcome").value("NEEDS_INFORMATION"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void rejectsUnboundedChatContext() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("message", "documents",
                                "contextSchemeIds", java.util.Collections.nCopies(6, schemeId)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void applicationReadWorksAcrossRealTransactionBoundaries() throws Exception {
        try {
        submit().andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].schemeName").isNotEmpty())
                .andExpect(jsonPath("$[0].history[0].newStatus").value("SUBMITTED"));
        mockMvc.perform(post("/api/v1/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"What is my application status?\"}"))
                .andExpect(status().isOk());
        } finally {
            jdbc.update("delete from application_status_history where application_id in (select id from applications where scheme_id = ?)", schemeId);
            jdbc.update("delete from applications where scheme_id = ?", schemeId);
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    void adminReviewEnforcesTransitionsAndWritesAudit() throws Exception {
        var result = submit().andExpect(status().isOk()).andReturn();
        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("applicationId").asText();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/applications/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\",\"remarks\":\"Checked\"}"))
                .andExpect(status().isConflict());
        for (String next : java.util.List.of("UNDER_REVIEW", "APPROVED")) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/applications/" + id + "/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(java.util.Map.of("status", next, "remarks", "Documents checked"))))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(next));
        }
        mockMvc.perform(get("/api/v1/admin/applications"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].history.length()").value(3));
    }

    @Test
    void disabledAccountCannotLogin() throws Exception {
        User user = new User();
        user.setEmail("disabled@jansetu.test");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setEnabled(false);
        userRepository.saveAndFlush(user);
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"disabled@jansetu.test\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions submit() throws Exception {
        return mockMvc.perform(post("/api/v1/applications").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("schemeId", schemeId))));
    }
}
