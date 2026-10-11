package com.jansetu.backend.source.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.OfficialSourceRepository;
import com.jansetu.backend.source.RetrievedSource;
import com.jansetu.backend.source.SourceFetcher;
import com.jansetu.backend.source.SourceSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    @Autowired
    private OfficialSourceRepository sources;
    @Autowired
    private SourceSnapshotRepository snapshots;
    @MockBean
    private SourceFetcher sourceFetcher;

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

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorCapturesAndReadsBackAnImmutableSnapshot() throws Exception {
        OfficialSource source = new OfficialSource();
        source.setName("MahaDBT evidence portal");
        source.setAuthority("Government of Maharashtra");
        source.setCanonicalUrl("https://mahadbt.maharashtra.gov.in/schemes");
        source.setNormalizedHost("mahadbt.maharashtra.gov.in");
        source = sources.saveAndFlush(source);
        byte[] content = "published scheme requirements".getBytes(StandardCharsets.UTF_8);
        when(sourceFetcher.fetch(any(URI.class))).thenReturn(new RetrievedSource(
                URI.create(source.getCanonicalUrl()), 200, "text/plain", content));

        mockMvc.perform(post("/api/v1/admin/sources/{sourceId}/snapshots", source.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentHash")
                        .value("sha256:cba48051e584c7860df7abe65dd45247309f5b796e0cbf073e5f1d224a516b9f"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/admin/sources/{sourceId}/snapshots", source.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contentExcerpt").value("published scheme requirements"));
        org.junit.jupiter.api.Assertions.assertArrayEquals(content,
                snapshots.findBySourceIdOrderByCapturedAtDesc(source.getId()).getFirst().getContentBytes());
    }

    private OfficialSourceRequest request() {
        return new OfficialSourceRequest("MahaDBT evidence portal", "Government of Maharashtra",
                "https://mahadbt.maharashtra.gov.in/schemes");
    }
}
