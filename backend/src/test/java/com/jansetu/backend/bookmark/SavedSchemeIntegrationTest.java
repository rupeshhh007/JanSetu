package com.jansetu.backend.bookmark;

import com.jansetu.backend.auth.entity.Role;
import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.scheme.entity.EligibilityCriteria;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SavedSchemeIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired SchemeRepository schemes;
    private UUID schemeId;

    @BeforeEach
    void fixtures() {
        for (String email : new String[]{"bookmark-one@example.test", "bookmark-two@example.test"}) {
            User user = new User();
            user.setEmail(email);
            user.setPasswordHash("not-used-by-mock-user");
            user.setRole(Role.CITIZEN);
            user.setEnabled(true);
            users.saveAndFlush(user);
        }
        Scheme scheme = new Scheme();
        scheme.setName("Test savings scheme");
        scheme.setDescription("Synthetic catalogue item");
        scheme.setState("India");
        scheme.setDepartment("Test");
        scheme.setStatus(SchemeStatus.PUBLISHED);
        scheme.setPublishedAt(Instant.now());
        EligibilityCriteria criteria = new EligibilityCriteria();
        criteria.setMaxFamilyIncome(new BigDecimal("999999"));
        scheme.setEligibilityCriteria(criteria);
        schemeId = schemes.saveAndFlush(scheme).getId();
    }

    @Test
    void anonymousCallCannotReadShortlist() throws Exception {
        mvc.perform(get("/api/v1/citizen/saved-schemes")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="bookmark-one@example.test", roles="CITIZEN")
    void saveIsIdempotentAndReturnsOnlyIds() throws Exception {
        mvc.perform(put("/api/v1/citizen/saved-schemes/" + schemeId)).andExpect(status().isNoContent());
        mvc.perform(put("/api/v1/citizen/saved-schemes/" + schemeId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/citizen/saved-schemes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value(schemeId.toString()));
    }

    @Test
    @WithMockUser(username="bookmark-one@example.test", roles="CITIZEN")
    void citizenCannotSeeOrRemoveAnotherCitizensBookmark() throws Exception {
        mvc.perform(put("/api/v1/citizen/saved-schemes/" + schemeId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/citizen/saved-schemes")
                .with(user("bookmark-two@example.test").roles("CITIZEN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/api/v1/citizen/saved-schemes/" + schemeId)
                .with(user("bookmark-two@example.test").roles("CITIZEN")))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/citizen/saved-schemes"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username="bookmark-one@example.test", roles="CITIZEN")
    void deletingUnknownBookmarkIsIdempotent() throws Exception {
        mvc.perform(delete("/api/v1/citizen/saved-schemes/" + schemeId))
                .andExpect(status().isNoContent());
        mvc.perform(put("/api/v1/citizen/saved-schemes/" + schemeId))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/v1/citizen/saved-schemes/" + schemeId))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/citizen/saved-schemes"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username="bookmark-one@example.test", roles="CITIZEN")
    void unpublishedSchemeCannotEnterCitizenShortlist() throws Exception {
        Scheme scheme = schemes.findById(schemeId).orElseThrow();
        scheme.setStatus(SchemeStatus.ARCHIVED);
        schemes.saveAndFlush(scheme);
        mvc.perform(put("/api/v1/citizen/saved-schemes/" + schemeId))
                .andExpect(status().isNotFound());
    }
}
