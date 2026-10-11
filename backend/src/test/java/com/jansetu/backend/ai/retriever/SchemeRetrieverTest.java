package com.jansetu.backend.ai.retriever;

import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemeRetrieverTest {

    @Test
    void ranksDescriptionAndBenefitTermsAndOmitsArchivedRecords() {
        Scheme scholarship = scheme("National Scholarship", "Support for students pursuing higher education",
                List.of("Tuition assistance"), SchemeStatus.PUBLISHED);
        Scheme archived = scheme("Old Scholarship", "student scholarship", List.of("Old benefit"), SchemeStatus.ARCHIVED);
        SchemeRetriever retriever = new SchemeRetriever(() -> List.of(archived, scholarship));

        List<SchemeRetriever.RetrievedScheme> results = retriever.retrieve("Need tuition help for higher education");

        assertEquals(1, results.size());
        assertEquals("National Scholarship", results.getFirst().scheme().getName());
        assertTrue(results.getFirst().matchedFields().contains("description"));
        assertTrue(results.getFirst().matchedFields().contains("benefits"));
    }

    @Test
    void contextualIdsCannotRevealDraftsAndPreserveSelectionOrder() {
        Scheme published = scheme("Published", "Support", List.of(), SchemeStatus.PUBLISHED);
        published.setId(java.util.UUID.randomUUID());
        Scheme draft = scheme("Private draft", "Support", List.of(), SchemeStatus.DRAFT);
        draft.setId(java.util.UUID.randomUUID());
        SchemeRetriever retriever = new SchemeRetriever(() -> List.of(draft, published));
        var results = retriever.retrieveContext(List.of(draft.getId(), published.getId()));
        assertEquals(1, results.size());
        assertEquals(published.getId(), results.getFirst().scheme().getId());
    }

    @Test
    void documentQuestionForNamedSchemeDoesNotReturnUnrelatedDocumentLists() {
        Scheme farmer = scheme("PM-KISAN Samman Nidhi", "Farmer support", List.of(), SchemeStatus.PUBLISHED);
        Scheme student = scheme("Student scholarship", "Education support", List.of(), SchemeStatus.PUBLISHED);
        student.setRequiredDocuments(List.of("Current admission documents"));
        SchemeRetriever retriever = new SchemeRetriever(() -> List.of(student, farmer));
        var results = retriever.retrieve("What documents do I need for PM-KISAN?");
        assertEquals(1, results.size());
        assertEquals("PM-KISAN Samman Nidhi", results.getFirst().scheme().getName());
    }

    private Scheme scheme(String name, String description, List<String> benefits, SchemeStatus status) {
        Scheme scheme = new Scheme();
        scheme.setName(name);
        scheme.setDescription(description);
        scheme.setBenefits(benefits);
        scheme.setRequiredDocuments(List.of());
        scheme.setState("India");
        scheme.setDepartment("Education");
        scheme.setStatus(status);
        return scheme;
    }
}
