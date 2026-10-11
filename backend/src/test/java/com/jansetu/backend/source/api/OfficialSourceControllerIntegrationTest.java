package com.jansetu.backend.source.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OfficialSourceControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CITIZEN")
    void citizenCannotRegisterAnOfficialSource() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sources").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorRegistersAnAllowlistedOfficialSource() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sources").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MahaDBT evidence portal"))
                .andExpect(jsonPath("$.normalizedHost").value("mahadbt.maharashtra.gov.in"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    private OfficialSourceRequest request() {
        return new OfficialSourceRequest("MahaDBT evidence portal", "Government of Maharashtra",
                "https://mahadbt.maharashtra.gov.in/schemes");
    }
}
