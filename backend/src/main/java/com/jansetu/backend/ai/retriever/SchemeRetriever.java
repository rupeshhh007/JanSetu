package com.jansetu.backend.ai.retriever;

import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SchemeRetriever {

    private final SchemeRepository schemeRepository;

    public SchemeRetriever(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    /**
     * Retrieves schemes relevant to the user's question.
     * Current implementation uses simple keyword matching.
     * This can later be replaced with embeddings / vector search.
     */
    public List<Scheme> retrieveRelevantSchemes(String question) {

        List<Scheme> allSchemes = schemeRepository.findAll();

        List<Scheme> relevantSchemes = new ArrayList<>();

        String query = question.toLowerCase();

        for (Scheme scheme : allSchemes) {

            String schemeName = scheme.getName().toLowerCase();

            // Exact scheme name match
            if (query.contains(schemeName)) {
                relevantSchemes.add(scheme);
                continue;
            }

            // Partial word match
            String[] words = schemeName.split("\\s+");

            for (String word : words) {

                if (word.length() < 4) {
                    continue;
                }

                if (query.contains(word)) {
                    relevantSchemes.add(scheme);
                    break;
                }
            }
        }

        return relevantSchemes;
    }

    /**
     * Formats retrieved schemes into AI-friendly context.
     */
    public String buildSchemeContext(List<Scheme> schemes) {

        if (schemes.isEmpty()) {
            return "No specific schemes referenced in the user's question.";
        }

        StringBuilder builder = new StringBuilder();

        builder.append("""
==================================================
RELEVANT SCHEMES FROM DATABASE
==================================================

""");

        for (Scheme scheme : schemes) {

            builder.append("Scheme Name: ")
                    .append(scheme.getName())
                    .append("\n\n");

            builder.append("Description:\n")
                    .append(scheme.getDescription())
                    .append("\n\n");

            builder.append("Eligibility Criteria:\n")
                    .append(scheme.getEligibilityCriteria())
                    .append("\n");

            builder.append("------------------------------------------\n\n");
        }

        return builder.toString();
    }

}