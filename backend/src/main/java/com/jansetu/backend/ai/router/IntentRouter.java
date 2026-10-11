package com.jansetu.backend.ai.router;

import org.springframework.stereotype.Component;
import java.util.Locale;
@Component
public class IntentRouter {
    public IntentType detectIntent(String question) {
        String q = question.toLowerCase(Locale.ROOT);
        if (q.contains("compare") || q.contains("difference")) return IntentType.COMPARE_SCHEMES;
        if (q.contains("document") || q.contains("paperwork")) return IntentType.DOCUMENTS;
        if (q.contains("status") || q.contains("my applications")) return IntentType.APPLICATION_STATUS;
        if (q.contains("eligible") || q.contains("eligibility")) return IntentType.ELIGIBILITY;
        if (q.contains("recommend") || q.contains("apply") || q.contains("my matches")) return IntentType.RECOMMENDATION;
        return IntentType.GENERAL;
    }
}
