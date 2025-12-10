package project.app.humanelogistics.service;

import project.app.humanelogistics.config.AIConfig;
import project.app.humanelogistics.config.AIConfig.ModelType;

public class GeminiSummaryGenerator implements SummaryGenerator {
    private final AIConfig aiService;

    public GeminiSummaryGenerator() {
        this.aiService = new AIConfig();
    }

    @Override
    public String generateSummary(String topic, int postCount, double avgSentiment, String topDamageType) {
        // Construct a structured prompt for the AI
        String prompt = String.format(
                "You are a disaster relief analyst. Write a concise 2-sentence executive summary for the topic '%s'.\n" +
                        "Data Context:\n" +
                        "- Total Reports: %d\n" +
                        "- Average Sentiment: %.2f (Scale: -1.0 Critical to 1.0 Hopeful)\n" +
                        "- Primary Damage Reported: %s\n\n" +
                        "Provide a summary that interprets the severity and the public mood."+
                        "Make it 4 sentence long give some suggestion for humanetarian logistic work ",
                topic, postCount, avgSentiment, topDamageType
        );

        // Use the UnifiedAIService to generate content
        // You can easily switch models here by changing ModelType.GEMINI_FLASH to another type
        String result = aiService.ask(ModelType.GEMINI_FLASH, prompt);

        if (result.startsWith("Error") || result.isEmpty()) {
            return "Unable to generate summary at this time.";
        }

        return result;
    }
}