package project.app.humanelogistics.preprocessing;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import project.app.humanelogistics.db.MongoPostRepository;
import project.app.humanelogistics.model.SocialPost;
import project.app.humanelogistics.service.SentimentAnalyzer;



import java.util.List;

public class SentimentGrade implements SentimentAnalyzer {

    // The client gets the API key from the environment variable `GEMINI_API_KEY`
    private final Client client;

    public SentimentGrade() {
        this.client = new Client();
    }

    @Override
    public SentimentType analyze(String text) {
        if (text == null || text.trim().isEmpty()) return SentimentType.NEUTRAL;

        try {
            // 1. Construct the prompt
            String prompt = "Classify the sentiment of this social media post about a disaster as POSITIVE, NEGATIVE, or NEUTRAL. Respond with ONLY the word.\n\nPost: " + text;

            // 2. Call the API using the Google GenAI SDK
            // We use the model specified in your snippet: gemini-2.5-flash
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            // 3. Parse Response
            // The SDK handles extracting the text, so we no longer need manual JSON parsing.
            String resultText = response.text();

            if (resultText != null) {
                String cleanText = resultText.trim().toUpperCase();

                if (cleanText.contains("POSITIVE")) return SentimentType.POSITIVE;
                if (cleanText.contains("NEGATIVE")) return SentimentType.NEGATIVE;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return SentimentType.NEUTRAL; // Fallback
    }

    @Override
    public double analyzeScore(String text) {
        SentimentType type = analyze(text);
        return type == SentimentType.POSITIVE ? 1.0 : (type == SentimentType.NEGATIVE ? -1.0 : 0.0);
    }

    // ============================================================================================
    // PREPROCESSOR RUNNER
    // Run this main method directly to batch-process all posts in the database.
    // This ensures that when you open the App, the charts load instantly.
    // ============================================================================================
    public static void main(String[] args) {
        System.out.println("--- Starting Batch Sentiment Preprocessing ---");

        // 1. Database Configuration (Same as in Controller)
        String connectionString = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        MongoPostRepository repository = new MongoPostRepository(connectionString, "storm_data", "posts");
        SentimentGrade analyzer = new SentimentGrade();

        // 2. Fetch all posts for the topic
        String topic = "Typhoon Yagi";
        List<SocialPost> posts = repository.findPostsByTopic(topic);
        System.out.println("Found " + posts.size() + " posts for topic: " + topic);

        int processedCount = 0;
        int skippedCount = 0;

        // 3. Iterate and Analyze
        for (SocialPost post : posts) {
            // Optimization: Only analyze if sentiment is missing
            if (post.getSentiment() == null || post.getSentiment().isEmpty()) {
                System.out.print("Analyzing post: " + truncate(post.getContent(), 30) + "... ");

                SentimentType type = analyzer.analyze(post.getContent());
                String sentimentStr = type.name();

                // Update the SocialPost model in memory
                post.setSentiment(sentimentStr);

                // Save update to MongoDB
                repository.updateSentiment(post, sentimentStr);
                System.out.println("-> " + sentimentStr);
                processedCount++;

                // Sleep to be polite to the API rate limit
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            } else {
                skippedCount++;
            }
        }

        System.out.println("--- Preprocessing Complete ---");
        System.out.println("Newly Analyzed: " + processedCount);
        System.out.println("Already Cached: " + skippedCount);
    }

    private static String truncate(String str, int width) {
        return (str.length() > width) ? str.substring(0, width - 3) + "..." : str;
    }
}