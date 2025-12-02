package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.*;

import java.util.ArrayList;
import java.util.List;

public class MongoMediaRepository implements MediaRepository {
    private final MongoCollection<Document> collection;

    public MongoMediaRepository(String connectionString, String dbName, String collName) {
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
        FindIterable<Document> docs = collection.find(new Document("topic", topic));

        for (Document doc : docs) {
            Media mediaItem = mapDocumentToMedia(doc);
            items.add(mediaItem);
        }
        return items;
    }

    private boolean findByContent(String content) {
        return collection.find(new Document("content", content)).first() != null;
    }

    private Media mapDocumentToMedia(Document doc) {
        String type = doc.getString("type");
        String url = doc.getString("url");

        // Construct basic object
        Media media;
        if ("news".equals(type)) {
            media = new News(
                    doc.getString("topic"),
                    doc.getString("content"),
                    doc.getString("source"),
                    url,
                    doc.getDate("timestamp")
            );
        } else {
            media = new SocialPost(
                    doc.getString("topic"),
                    doc.getString("content"),
                    url,
                    doc.getDate("timestamp"),
                    doc.getList("comments", String.class)
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