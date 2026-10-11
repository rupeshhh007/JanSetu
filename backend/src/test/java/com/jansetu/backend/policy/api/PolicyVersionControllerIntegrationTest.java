package com.jansetu.backend.policy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.policy.domain.BooleanMatchRule;
import com.jansetu.backend.policy.domain.FactKey;
import com.jansetu.backend.policy.registry.PolicySourceReference;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PolicyVersionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SchemeRepository schemes;

    private UUID schemeId;
    private PolicyVersionRequest request;

    @BeforeEach
    void setUp() {
        Scheme scheme = new Scheme();
        scheme.setName("Policy API fixture " + UUID.randomUUID());
        scheme.setDescription("A fixture for administrative policy API verification.");
        scheme.setState("Maharashtra");
        scheme.setDepartment("Test department");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setEligibilityCriteria(new EligibilityCriteria());
        scheme.setBenefits(List.of());
        scheme.setRequiredDocuments(List.of());
        schemeId = schemes.saveAndFlush(scheme).getId();
        request = new PolicyVersionRequest(LocalDate.of(2026, 10, 1),
                new BooleanMatchRule("student-rule", "Recorded student requirement", FactKey.IS_STUDENT, true),
                List.of(new PolicySourceReference("https://mahadbt.maharashtra.gov.in/scheme/example",
                        "MahaDBT fixture", "sha256:fixture-content", "Recorded requirement excerpt.")));
    }

    @Test
    @WithMockUser(username = "citizen@jansetu.test", roles = "CITIZEN")
    void citizenCannotAccessThePolicyRegistry() throws Exception {
        mockMvc.perform(get(baseUrl()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operator@jansetu.test", roles = "ADMIN")
    void administratorCanCreateAValidatedPolicyDraft() throws Exception {
        mockMvc.perform(post(baseUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.policyRule.type").value("booleanMatch"))
                .andExpect(jsonPath("$.sourceReferences[0].contentHash").value("sha256:fixture-content"));
    }

    private String baseUrl() {
        return "/api/v1/admin/schemes/" + schemeId + "/policy-versions";
    }
}
