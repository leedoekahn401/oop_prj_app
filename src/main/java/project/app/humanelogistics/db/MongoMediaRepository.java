package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.regex.Pattern;

public class MongoMediaRepository implements MediaRepository {
    private final MongoCollection<Document> collection;

    public MongoMediaRepository(MongoClient client, String dbName, String collectionName) {
        this.collection = client.getDatabase(dbName).getCollection(collectionName);
    }

    @Override
    public void save(MediaAnalysis analysis) {
        Media item = analysis.getMedia();
        if(findByContent(item.getContent())) return;

        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("url", item.getUrl())
                .append("timestamp", item.getTimestamp());

        // Save Analysis Fields from the wrapper
        doc.append("sentiment", analysis.getSentiment().getValue());
        doc.append("damageType", analysis.getDamageCategory().name());

        if (item instanceof News) {
            doc.append("source", ((News) item).getSource());
            doc.append("type", "news");
        } else if (item instanceof SocialPost) {
            doc.append("comments", ((SocialPost) item).getComments());
            doc.append("type", "social_post");
        }

        collection.insertOne(doc);
    }

    @Override
    public void updateAnalysis(MediaAnalysis analysis) {
        Media item = analysis.getMedia();
        collection.updateOne(
                new Document("content", item.getContent()), // Match by content ID instead of topic to be safe
                new Document("$set", new Document()
                        .append("sentiment", analysis.getSentiment().getValue())
                        .append("damageType", analysis.getDamageCategory().name())
                )
        );
    }

    @Override
    public List<MediaAnalysis> findByTopic(String topic) {
        List<MediaAnalysis> items = new ArrayList<>();

        // FIX: Use Regex for case-insensitive and whitespace-tolerant matching
        // This ensures "Typhoon Yagi" matches "typhoon yagi" or "Typhoon Yagi "
        Pattern regex = Pattern.compile("^" + Pattern.quote(topic.trim()) + "$", Pattern.CASE_INSENSITIVE);
        FindIterable<Document> docs = collection.find(new Document("topic", regex));

        for (Document doc : docs) {
            try {
                MediaAnalysis analysis = mapDocumentToAnalysis(doc);
                if (analysis != null) {
                    items.add(analysis);
                }
            } catch (Exception e) {
                // Silently skip corrupted docs to prevent crashing the whole list
                System.err.println("Skipping doc: " + e.getMessage());
            }
        }
        return items;
    }

    private boolean findByContent(String content) {
        return collection.find(new Document("content", content)).first() != null;
    }

    private MediaAnalysis mapDocumentToAnalysis(Document doc) {
        try {
            // 1. Reconstruct Media
            String type = doc.getString("type");
            String url = doc.getString("url");
            // If URL is missing in DB (common in social posts), default to empty
            if (url == null) url = "";

            String topic = doc.getString("topic");
            String content = doc.getString("content");

            // Robust Date Handling
            Date timestamp = doc.getDate("timestamp");
            if (timestamp == null) {
                // Fallback: Try to parse from Object ID or use current time to prevent null errors
                timestamp = new Date();
            }

            Media media;
            if ("news".equals(type)) {
                String source = doc.getString("source");
                if (source == null) source = "Unknown Source";
                media = new News(topic, content, source, url, timestamp);
            } else {
                // Default to SocialPost if type is missing or matches "social_post"
                List<String> comments = doc.getList("comments", String.class);
                if (comments == null) comments = new ArrayList<>();
                media = new SocialPost(topic, content, url, timestamp, comments);
            }

            // 2. Reconstruct Analysis
            Double sentimentVal = doc.getDouble("sentiment");
            // Handle missing sentiment by defaulting to 0.0
            if (sentimentVal == null) sentimentVal = 0.0;

            String damageStr = doc.getString("damageType");

            return new MediaAnalysis(
                    media,
                    SentimentScore.of(sentimentVal),
                    DamageCategory.fromText(damageStr)
            );
        } catch (Exception e) {
            // Log specific error for debugging
            System.err.println("Mapping Error for ID " + doc.get("_id") + ": " + e.getMessage());
            return null;
        }
    }
}