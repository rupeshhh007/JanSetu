package com.jansetu.backend.ai.service;

import com.jansetu.backend.ai.dto.ChatCard;
import com.jansetu.backend.ai.dto.ChatResult;
import com.jansetu.backend.ai.dto.Citation;
import com.jansetu.backend.ai.retriever.SchemeRetriever;
import com.jansetu.backend.ai.router.IntentType;
import com.jansetu.backend.scheme.entity.Scheme;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Local answer path used by default. It only exposes facts in approved scheme
 * records and therefore remains useful when no model key or network is present.
 */
@Service
public class GroundedFallbackService {

    public ChatResult answer(
            String question,
            IntentType intent,
            List<SchemeRetriever.RetrievedScheme> retrieved,
            String applicationSummary
    ) {
        if (intent == IntentType.APPLICATION_STATUS) {
            return new ChatResult(
                    "## Your application status\n\n" + applicationSummary,
                    List.of(), List.of(),
                    List.of("Which documents are required for my pending application?"), true
            );
        }
        if (retrieved.isEmpty()) {
            return noMatch(intent);
        }

        return switch (intent) {
            case DOCUMENTS -> documents(retrieved);
            case COMPARE_SCHEMES -> compare(retrieved);
            case ELIGIBILITY -> eligibility(retrieved);
            default -> schemeFacts(retrieved);
        };
    }

    private ChatResult documents(List<SchemeRetriever.RetrievedScheme> results) {
        StringBuilder text = new StringBuilder("## Recorded documents\n\n");
        List<ChatCard> cards = new ArrayList<>();
        for (SchemeRetriever.RetrievedScheme result : results) {
            Scheme scheme = result.scheme();
            List<String> documents = safe(scheme.getRequiredDocuments());
            text.append("### ").append(scheme.getName()).append("\n");
            if (documents.isEmpty()) {
                text.append("No document list has been verified in the JanSetu catalogue yet. Check the official source.\n\n");
            } else {
                documents.forEach(document -> text.append("- ").append(document).append("\n"));
                text.append("\n");
            }
            cards.add(card(scheme, documents, "Open official source"));
        }
        return result(text, results, cards, List.of("Show schemes I am eligible for"));
    }

    private ChatResult compare(List<SchemeRetriever.RetrievedScheme> results) {
        if (results.size() < 2) {
            return new ChatResult(
                    "I found one relevant scheme: **" + results.getFirst().scheme().getName()
                            + "**. Name another scheme to compare it with.",
                    citations(results), List.of(card(results.getFirst().scheme(), highlights(results.getFirst().scheme()), "Open official source")),
                    List.of("Compare this with another scheme"), true
            );
        }
        StringBuilder text = new StringBuilder("## Comparison from recorded catalogue data\n\n");
        List<ChatCard> cards = new ArrayList<>();
        for (SchemeRetriever.RetrievedScheme result : results) {
            Scheme scheme = result.scheme();
            text.append("### ").append(scheme.getName()).append("\n")
                    .append("- **Jurisdiction:** ").append(orUnknown(scheme.getState())).append("\n")
                    .append("- **Department:** ").append(orUnknown(scheme.getDepartment())).append("\n")
                    .append("- **Benefit:** ").append(joinOrUnknown(scheme.getBenefits())).append("\n")
                    .append("- **Recorded documents:** ").append(joinOrUnknown(scheme.getRequiredDocuments())).append("\n\n");
            cards.add(card(scheme, highlights(scheme), "Open official source"));
        }
        return result(text, results, cards, List.of("Which of these am I eligible for?"));
    }

    private ChatResult eligibility(List<SchemeRetriever.RetrievedScheme> results) {
        StringBuilder text = new StringBuilder("## Eligibility check\n\n")
                .append("JanSetu evaluates the rules saved in your profile on the server. "
                        + "I cannot make a final government eligibility decision in chat.\n\n");
        List<ChatCard> cards = new ArrayList<>();
        for (SchemeRetriever.RetrievedScheme result : results) {
            Scheme scheme = result.scheme();
            text.append("### ").append(scheme.getName()).append("\n")
                    .append("Recorded criteria: ").append(scheme.getEligibilityCriteria()).append("\n\n");
            cards.add(card(scheme, highlights(scheme), "Open official source"));
        }
        return result(text, results, cards, List.of("Which schemes should I apply for first?"));
    }

    private ChatResult schemeFacts(List<SchemeRetriever.RetrievedScheme> results) {
        StringBuilder text = new StringBuilder("## What I found in the JanSetu catalogue\n\n");
        List<ChatCard> cards = new ArrayList<>();
        for (SchemeRetriever.RetrievedScheme result : results) {
            Scheme scheme = result.scheme();
            text.append("### ").append(scheme.getName()).append("\n")
                    .append(scheme.getDescription()).append("\n")
                    .append("- **Jurisdiction:** ").append(orUnknown(scheme.getState())).append("\n");
            if (!safe(scheme.getBenefits()).isEmpty()) {
                text.append("- **Recorded benefits:** ").append(String.join("; ", scheme.getBenefits())).append("\n");
            }
            text.append("\n");
            cards.add(card(scheme, highlights(scheme), "Open official source"));
        }
        return result(text, results, cards,
                List.of("What documents do I need?", "Am I eligible for these schemes?"));
    }

    private ChatResult noMatch(IntentType intent) {
        String direction = switch (intent) {
            case DOCUMENTS -> "Name a scheme and I’ll show the documents recorded for it.";
            case COMPARE_SCHEMES -> "Name two schemes and I’ll compare their recorded benefits and documents.";
            case ELIGIBILITY -> "Name a scheme or ask for personalized recommendations.";
            default -> "Try a scheme name, benefit, department, or ask for personalized recommendations.";
        };
        return new ChatResult(
                "I don’t have a verified matching record in the current JanSetu catalogue. " + direction,
                List.of(), List.of(),
                List.of("Which schemes should I apply for first?", "What documents do I need?"), true
        );
    }

    private ChatResult result(StringBuilder text, List<SchemeRetriever.RetrievedScheme> results,
                              List<ChatCard> cards, List<String> followUps) {
        text.append("Verify details and final eligibility on the cited official source.\n");
        return new ChatResult(text.toString(), citations(results), cards, followUps, true);
    }

    private List<Citation> citations(List<SchemeRetriever.RetrievedScheme> results) {
        return results.stream().map(result -> citation(result.scheme())).toList();
    }

    private Citation citation(Scheme scheme) {
        Instant verifiedAt = scheme.getLastVerifiedAt();
        return new Citation(scheme.getName(),
                scheme.getSourceName() == null ? "JanSetu catalogue" : scheme.getSourceName(),
                scheme.getOfficialUrl(), verifiedAt == null ? null : verifiedAt.toString());
    }

    private ChatCard card(Scheme scheme, List<String> highlights, String actionLabel) {
        String schemeId = scheme.getId() == null ? null : scheme.getId().toString();
        return new ChatCard("scheme", scheme.getName(), scheme.getDescription(), schemeId,
                highlights, actionLabel, scheme.getOfficialUrl());
    }

    private List<String> highlights(Scheme scheme) {
        List<String> output = new ArrayList<>();
        output.addAll(safe(scheme.getBenefits()));
        output.addAll(safe(scheme.getRequiredDocuments()));
        return output.stream().limit(4).toList();
    }

    private static List<String> safe(List<String> values) { return values == null ? List.of() : values; }
    private static String joinOrUnknown(List<String> values) { return safe(values).isEmpty() ? "Not recorded" : String.join("; ", values); }
    private static String orUnknown(String value) { return value == null || value.isBlank() ? "Not recorded" : value; }
}
