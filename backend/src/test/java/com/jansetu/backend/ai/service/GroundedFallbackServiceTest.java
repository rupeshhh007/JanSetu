package com.jansetu.backend.ai.service;

import com.jansetu.backend.ai.dto.ChatResult;
import com.jansetu.backend.ai.retriever.SchemeRetriever;
import com.jansetu.backend.ai.router.IntentType;
import com.jansetu.backend.scheme.entity.Scheme;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundedFallbackServiceTest {

    @Test
    void returnsRecordedDocumentsAndSourceWithoutCallingAModel() {
        GroundedFallbackService service = new GroundedFallbackService();
        Scheme scheme = new Scheme();
        scheme.setName("Demo Education Support");
        scheme.setDescription("Support for eligible students.");
        scheme.setRequiredDocuments(List.of("Identity proof", "Income certificate"));
        scheme.setBenefits(List.of("Tuition assistance"));
        scheme.setOfficialUrl("https://example.gov.in/demo");
        scheme.setSourceName("Demo Government Department");

        ChatResult result = service.answer("documents", IntentType.DOCUMENTS,
                List.of(new SchemeRetriever.RetrievedScheme(scheme, 10, List.of("documents"))), "");

        assertTrue(result.fallback());
        assertTrue(result.response().contains("Income certificate"));
        assertEquals("https://example.gov.in/demo", result.citations().getFirst().url());
        assertEquals("scheme", result.cards().getFirst().type());
    }
}
