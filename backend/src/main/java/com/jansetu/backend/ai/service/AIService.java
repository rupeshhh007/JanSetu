package com.jansetu.backend.ai.service;

import com.jansetu.backend.ai.context.ContextBuilder;
import com.jansetu.backend.ai.context.UserContext;
import com.jansetu.backend.ai.dto.ChatRequest;
import com.jansetu.backend.ai.dto.ExplainRequest;
import com.jansetu.backend.ai.prompt.PromptBuilder;
import com.jansetu.backend.ai.recommendation.RecommendationAIService;
import com.jansetu.backend.ai.retriever.SchemeRetriever;
import com.jansetu.backend.ai.router.IntentRouter;
import com.jansetu.backend.ai.router.IntentType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private final ChatClient chatClient;

    private final PromptBuilder promptBuilder;

    private final ContextBuilder contextBuilder;

    private final SchemeRetriever schemeRetriever;

    private final IntentRouter intentRouter;

    private final RecommendationAIService recommendationAIService;

    public AIService(
            ChatClient.Builder builder,
            PromptBuilder promptBuilder,
            ContextBuilder contextBuilder,
            SchemeRetriever schemeRetriever,
            IntentRouter intentRouter,
            RecommendationAIService recommendationAIService
    ) {
        this.chatClient = builder.build();
        this.promptBuilder = promptBuilder;
        this.contextBuilder = contextBuilder;
        this.schemeRetriever = schemeRetriever;
        this.intentRouter = intentRouter;
        this.recommendationAIService = recommendationAIService;
    }

    public UserContext getContext() {
        return contextBuilder.buildContext();
    }

    public String explainEligibility(ExplainRequest request) {

        String systemPrompt = """
                You are JanSetu AI.

                Explain why the citizen is eligible.

                Rules:
                - Only use supplied information.
                - Never hallucinate.
                - Use bullet points.
                - Keep the explanation under four sentences.
                """;

        String userPrompt = """
                User Profile:
                %s

                Scheme:
                %s

                Description:
                %s

                Matched Criteria:
                %s
                """
                .formatted(
                        request.userProfileSummary(),
                        request.schemeName(),
                        request.schemeDescription(),
                        String.join(", ", request.matchedCriteria())
                );

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    public String chat(ChatRequest request) {

        String question = request.message().trim();

        // Handle greetings without calling the LLM
        if (isGreeting(question)) {
            return """
👋 Hello! I'm JanSetu AI.

I can help you with:

• Finding eligible government schemes

• Comparing schemes

• Explaining eligibility

• Checking your application status

• Required documents

How can I help you today?
""";
        }

        IntentType intent = intentRouter.detectIntent(question);

        System.out.println("====================================");
        System.out.println("Detected Intent : " + intent);
        System.out.println("Question        : " + question);
        System.out.println("====================================");

        return switch (intent) {

            case RECOMMENDATION ->
                    recommendationAIService.generateRecommendation();

            case APPLICATION_STATUS ->
                    contextBuilder.buildContext().applications();

            default ->
                    askLLM(question);
        };
    }

    private String askLLM(String question) {

        UserContext context =
                contextBuilder.buildContext();

        String schemeContext =
                schemeRetriever.buildSchemeContext(
                        schemeRetriever.retrieveRelevantSchemes(question)
                );

        String prompt =
                promptBuilder.buildChatPrompt(
                        context.profile(),
                        context.recommendations(),
                        context.applications(),
                        schemeContext,
                        question
                );

        return chatClient
                .prompt()
                .system("""
                        You are JanSetu AI.

                        You are an AI assistant for JanSetu.

                        IMPORTANT RULES

                        • The supplied context is the ONLY source of truth.

                        • Never invent schemes.

                        • Never invent eligibility.

                        • Never invent application status.

                        • Never invent benefits.

                        • Never mention government schemes that are NOT present
                          in the supplied context.

                        • If information is unavailable,
                          clearly state that it is unavailable.

                        • Use Markdown.

                        • Prefer bullet points.

                        • Keep answers concise.

                        • If comparing schemes,
                          compare ONLY the supplied schemes.
                        """)
                .user(prompt)
                .call()
                .content();
    }

    private boolean isGreeting(String message) {

        String q = message.trim().toLowerCase();

        return q.equals("hi")
                || q.equals("hello")
                || q.equals("hey")
                || q.equals("hii")
                || q.equals("heyy")
                || q.equals("good morning")
                || q.equals("good afternoon")
                || q.equals("good evening");
    }
}