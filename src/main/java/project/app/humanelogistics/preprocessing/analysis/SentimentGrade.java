package project.app.humanelogistics.preprocessing.analysis;

import project.app.humanelogistics.config.AIConfig;
import project.app.humanelogistics.config.AIConfig.ModelType;

public class SentimentGrade implements SentimentAnalyzer {

    private final AIConfig aiService;

    public SentimentGrade() {
        this.aiService = new AIConfig();
    }

    @Override
    public double analyzeScore(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;

        String prompt = "Analyze the sentiment of this text regarding a disaster. " +
                "Rate it on a scale from -1.0 (extremely negative/critical) to 1.0 (extremely positive/hopeful). " +
                "0.0 is neutral. Respond with ONLY the numeric value (e.g., -0.5, 0.8, 0.0). No words.\n\nPost: " + text;

        // Use the Unified Service
        String resultText = aiService.ask(ModelType.GEMINI_FLASH, prompt);

        try {
            if (resultText != null && !resultText.startsWith("Error")) {
                // Clean up any stray markdown or whitespace
                resultText = resultText.replaceAll("[^\\d.-]", "");
                if (resultText.isEmpty()) return 0.0;
                return Double.parseDouble(resultText.trim());
            }
        } catch (Exception e) {
            System.err.println("Sentiment Parsing Failure: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public SentimentType analyze(String text) {
        double score = analyzeScore(text);
        if (score > 0.1) return SentimentType.POSITIVE;
        if (score < -0.1) return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }

    // === MAIN METHOD FOR TESTING ===
    public static void main(String[] args) {
        System.out.println("--- Testing SentimentGrade via UnifiedAIService ---");
        SentimentGrade analyzer = new SentimentGrade();

        String text = "Typhoon Yagi destroyed 500 homes, leaving thousands homeless.";
        System.out.println("Analyzing: " + text);

        double score = analyzer.analyzeScore(text);
        System.out.printf("Result: %.2f%n", score);
    }
}