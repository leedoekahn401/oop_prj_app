package project.app.humanelogistics.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import project.app.humanelogistics.config.AppConfig;

public class GeminiSummaryGenerator implements SummaryGenerator {
    private final Client client;

    public GeminiSummaryGenerator() {
        String apiKey = AppConfig.getInstance().getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            this.client = Client.builder().apiKey(apiKey).build();
        } else {
            this.client = null;
        }
    }

    @Override
    public String generateSummary(String topic, int postCount, double avgSentiment, String topDamageType) {
        if (client == null) return "AI Summary unavailable (No API Key).";
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

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            return text != null ? text.trim() : "Analysis failed.";
        } catch (Exception e) {
            System.err.println("Summary Gen Error: " + e.getMessage());
            return "Unable to generate summary at this time.";
        }
    }
}