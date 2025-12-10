package project.app.humanelogistics.config;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class AIConfig {

    // Define supported models here so you don't have magic strings everywhere
    public enum ModelType {
        GEMINI_FLASH("gemini-2.5-flash-lite"),
        GEMINI_PRO("gemini-2.0-pro"),
        GPT_4("gpt-4"),
        CLAUDE_3("claude-3-opus");

        private final String modelId;
        ModelType(String modelId) { this.modelId = modelId; }
        public String getModelId() { return modelId; }
    }

    private final Client geminiClient;
    // private final OpenAiService openAiService; // Future

    public AIConfig() {
        String apiKey = AppConfig.getInstance().getApiKey();
        this.geminiClient = (apiKey != null && !apiKey.isEmpty())
                ? Client.builder().apiKey(apiKey).build()
                : null;

        // Initialize other clients here in the future
        // this.openAiService = new OpenAiService(AppConfig.getInstance().getOpenAIKey());
    }

    /**
     * The Master Method: You choose the model, you give the prompt.
     */
    public String ask(ModelType model, String prompt) {
        if (model.name().startsWith("GEMINI")) {
            return askGemini(model.getModelId(), prompt);
        }
        else if (model.name().startsWith("GPT")) {
            return "ChatGPT implementation coming soon..."; // askChatGPT(prompt);
        }
        return "Unknown Model";
    }

    private String askGemini(String modelId, String prompt) {
        if (geminiClient == null) return "Error: Gemini API Key missing.";
        try {
            GenerateContentResponse response = geminiClient.models.generateContent(modelId, prompt, null);
            return response.text() != null ? response.text().trim() : "";
        } catch (Exception e) {
            System.err.println("Gemini Error: " + e.getMessage());
            return "";
        }
    }
}