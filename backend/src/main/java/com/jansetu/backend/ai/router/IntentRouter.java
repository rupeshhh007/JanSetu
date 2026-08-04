package com.jansetu.backend.ai.router;

import org.springframework.stereotype.Component;

@Component
public class IntentRouter {

    public IntentType detectIntent(String question) {

        String q = question.toLowerCase();

        if (q.contains("compare"))
            return IntentType.COMPARE_SCHEMES;

        if (q.contains("status"))
            return IntentType.APPLICATION_STATUS;

        if (q.contains("eligible")
                || q.contains("eligibility"))
            return IntentType.ELIGIBILITY;

        if (q.contains("document"))
            return IntentType.DOCUMENTS;

        if (q.contains("recommend")
                || q.contains("apply"))
            return IntentType.RECOMMENDATION;

        return IntentType.GENERAL;
    }

}