package project.app.humanelogistics.preprocessing.analysis;

import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.config.AIConfig;
import project.app.humanelogistics.config.AIConfig.ModelType;

public class DamageClassifier implements ContentClassifier {

    private final AIConfig aiService;

    public DamageClassifier() {
        this.aiService = new AIConfig();
    }

    @Override
    public DamageCategory classify(String text) {
        if (text == null || text.trim().isEmpty()) return DamageCategory.UNKNOWN;

        // Truncate extremely long articles to avoid token limits
        String safeText = text.length() > 3000 ? text.substring(0, 3000) : text;

        String prompt = "Classify this text regarding a disaster into EXACTLY ONE of these categories:\n" +
                "- AFFECTED_PEOPLE (deaths, injuries, missing, evacuees)\n" +
                "- ECONOMIC_IMPACT (farms destroyed, factories closed, jobs lost)\n" +
                "- HOUSING_DAMAGE (roofs blown off, flooded homes, collapsed walls)\n" +
                "- LOSS_OF_BELONGINGS (vehicles, clothes, electronics lost)\n" +
                "- INFRASTRUCTURE_DAMAGE (bridges, roads, power lines, internet)\n" +
                "- OTHER (if it mentions damage but doesn't fit above)\n" +
                "- UNKNOWN (if it is general news or not about specific damage)\n\n" +
                "Return ONLY the category name (e.g., HOUSING_DAMAGE). No other text.\n\n" +
                "Text: " + safeText;

        // Use the Unified Service
        String resultText = aiService.ask(ModelType.GEMINI_FLASH, prompt);

        if (resultText != null && !resultText.startsWith("Error")) {
            return DamageCategory.fromText(resultText.trim());
        }

        return DamageCategory.UNKNOWN;
    }
}