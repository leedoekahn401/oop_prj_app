package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class MongoMediaRepository implements MediaRepository {
    private final MongoCollection<Document> collection;
    private String connectionString = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    public MongoMediaRepository(String dbName, String collName) {
        MongoClient client = MongoClients.create(connectionString);
        this.collection = client.getDatabase(dbName).getCollection(collName);
    }

    @Override
    public void save(Media item) {
        if(findByContent(item.getContent())) return;

        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("url", item.getUrl())
                .append("timestamp", item.getTimestamp());

        // Map Analysis Results to DB fields
        doc.append("sentiment", item.getSentiment().getValue());
        doc.append("damageType", item.getDamageCategory().name());

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
    public void updateAnalysis(Media item) {
        collection.updateOne(
                new Document("content", item.getContent()).append("topic", item.getTopic()),
                new Document("$set", new Document()
                        .append("sentiment", item.getSentiment().getValue())
                        .append("damageType", item.getDamageCategory().name())
                )
        );
    }

    @Override
    public void updateSentiment(Media item, Double sentiment) {
        // Forward compatibility wrapper
        item.addAnalysisResult("sentiment", SentimentScore.of(sentiment));
        updateAnalysis(item);
    }

    @Override
    public List<Media> findByTopic(String topic) {
        List<Media> items = new ArrayList<>();
        // Use regex for case-insensitive matching to be safer
        // Pattern: ^topic$ with 'i' option
        // Or just simple equality if performance is key
        FindIterable<Document> docs = collection.find(new Document("topic", topic));

        for (Document doc : docs) {
            try {
                Media mediaItem = mapDocumentToMedia(doc);
                if (mediaItem != null) {
                    items.add(mediaItem);
                }
            } catch (Exception e) {
                System.err.println("Skipping corrupted document: " + doc.get("_id") + " - " + e.getMessage());
            }
        }
        return items;
    }

    private boolean findByContent(String content) {
        return collection.find(new Document("content", content)).first() != null;
    }

    private Media mapDocumentToMedia(Document doc) {
        String type = doc.getString("type");
        String url = doc.getString("url");

        // --- SAFE DATA FETCHING (The Fix) ---
        // Provide defaults if fields are missing to prevent NPE in Model constructor
        String topic = doc.getString("topic");
        if (topic == null) topic = "Unknown Topic";

        String content = doc.getString("content");
        if (content == null) content = "[No Content Available]";

        Date timestamp = doc.getDate("timestamp");
        if (timestamp == null) timestamp = new Date();

        // Construct basic object
        Media media;
        if ("news".equals(type)) {
            String source = doc.getString("source");
            if (source == null) source = "Unknown Source";

            media = new News(
                    topic,
                    content,
                    source,
                    url,
                    timestamp
            );
        } else {
            // Safe cast for list
            List<String> comments = doc.getList("comments", String.class);
            if (comments == null) comments = new ArrayList<>();

            media = new SocialPost(
                    topic,
                    content,
                    url,
                    timestamp,
                    comments
            );
        }

        // Hydrate Analysis Results (OCP)
        Double sentimentVal = doc.getDouble("sentiment");
        if (sentimentVal == null) sentimentVal = 0.0;

        String damageStr = doc.getString("damageType");

        media.addAnalysisResult("sentiment", SentimentScore.of(sentimentVal));
        media.addAnalysisResult("damageCategory", DamageCategory.fromText(damageStr));

        return media;
    }
}