package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

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
                new Document("content", item.getContent()).append("topic", item.getTopic()),
                new Document("$set", new Document()
                        .append("sentiment", analysis.getSentiment().getValue())
                        .append("damageType", analysis.getDamageCategory().name())
                )
        );
    }

    @Override
    public List<MediaAnalysis> findByTopic(String topic) {
        List<MediaAnalysis> items = new ArrayList<>();
        FindIterable<Document> docs = collection.find(new Document("topic", topic));

        for (Document doc : docs) {
            try {
                MediaAnalysis analysis = mapDocumentToAnalysis(doc);
                if (analysis != null) {
                    items.add(analysis);
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

    private MediaAnalysis mapDocumentToAnalysis(Document doc) {
        // 1. Reconstruct Media
        String type = doc.getString("type");
        String url = doc.getString("url");
        String topic = doc.getString("topic");
        String content = doc.getString("content");
        Date timestamp = doc.getDate("timestamp");
        if (timestamp == null) timestamp = new Date();

        Media media;
        if ("news".equals(type)) {
            String source = doc.getString("source");
            if (source == null) source = "Unknown Source";
            media = new News(topic, content, source, url, timestamp);
        } else {
            List<String> comments = doc.getList("comments", String.class);
            if (comments == null) comments = new ArrayList<>();
            media = new SocialPost(topic, content, url, timestamp, comments);
        }

        // 2. Reconstruct Analysis
        Double sentimentVal = doc.getDouble("sentiment");
        if (sentimentVal == null) sentimentVal = 0.0;

        String damageStr = doc.getString("damageType");

        return new MediaAnalysis(
                media,
                SentimentScore.of(sentimentVal),
                DamageCategory.fromText(damageStr)
        );
    }
}