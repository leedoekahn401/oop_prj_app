package project.app.humanelogistics.preprocessing;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.model.Media; // Updated import
import project.app.humanelogistics.service.SentimentAnalyzer;

import java.util.List;

public class SentimentGrade implements SentimentAnalyzer {

    private final Client client;

    public SentimentGrade() {
        // Automatically uses GOOGLE_API_KEY from Environment Variables
        this.client = new Client();
    }

    @Override
    public double analyzeScore(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;

        try {
            String prompt = "Analyze the sentiment of this text regarding a disaster. " +
                    "Rate it on a scale from -1.0 (extremely negative/critical) to 1.0 (extremely positive/hopeful). " +
                    "0.0 is neutral. Respond with ONLY the numeric value (e.g., -0.5, 0.8, 0.0).\n\nPost: " + text;

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            String resultText = response.text();
            if (resultText != null) {
                return Double.parseDouble(resultText.trim());
            }

        } catch (Exception e) {
            throw new RuntimeException("Gemini API Failure: " + e.getMessage(), e);
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

    // Main method for batch processing
    public static void main(String[] args) {
        System.out.println("--- Starting Sentiment Score Preprocessor (-1 to 1) ---");

        String connectionString = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        // Use MongoMediaRepository instead of MongoPostRepository
        MongoMediaRepository repository = new MongoMediaRepository(connectionString, "storm_data", "posts");
        SentimentGrade analyzer = new SentimentGrade();

        String topic = "Typhoon Yagi";

        // FIX: Changed 'findPostsByTopic' to 'findByTopic'
        // Also updated List<SocialPost> to List<Media> (or MediaItem if you haven't fully renamed yet)
        List<Media> posts = repository.findByTopic(topic);
        System.out.println("Found " + posts.size() + " posts for topic: " + topic);

        int processedCount = 0;

        for (Media post : posts) {
            boolean isMissing = post.getSentiment() == null || post.getSentiment().isEmpty();
            boolean isOldFormat = post.getSentiment() != null && post.getSentiment().matches(".*[a-zA-Z]+.*");
            boolean isZero = "0.0".equals(post.getSentiment()) || "0".equals(post.getSentiment());

            if (isMissing || isOldFormat || isZero) {
                System.out.print("Scoring: " + truncate(post.getContent(), 30) + "... ");

                try {
                    double score = analyzer.analyzeScore(post.getContent());
                    String scoreStr = String.valueOf(score);

                    post.setSentiment(scoreStr);
                    repository.updateSentiment(post, scoreStr);

                    System.out.println("-> " + scoreStr);
                    processedCount++;

                    // Polite delay
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Failed to analyze post: " + e.getMessage());
                }
            }
        }
        System.out.println("--- Preprocessing Complete. Newly Scored: " + processedCount + " ---");
    }

    private static String truncate(String str, int width) {
        return (str.length() > width) ? str.substring(0, width - 3) + "..." : str;
    }
}