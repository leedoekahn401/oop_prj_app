package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.News;
import project.app.humanelogistics.model.SocialPost;
import java.util.ArrayList;
import java.util.List;

public class MongoMediaRepository implements MediaRepository {
    private final MongoCollection<Document> collection;

    public MongoMediaRepository(String connectionString, String dbName, String collName) {
        try {
            MongoClient client = MongoClients.create(connectionString);
            MongoDatabase db = client.getDatabase(dbName);
            this.collection = db.getCollection(collName);
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    @Override
    public void save(Media item) {
        if(findByContent(item.getContent())) return; // Deduplicate

        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("timestamp", item.getTimestamp())
                .append("sentiment", item.getSentiment());

        // Polymorphic saving
        if (item instanceof News) {
            News news = (News) item;
            doc.append("source", news.getSource());
            doc.append("url", news.getUrl());
            doc.append("type", "news");
        } else if (item instanceof SocialPost) {
            SocialPost post = (SocialPost) item;
            doc.append("comments", post.getComments());
            doc.append("type", "social_post");
        } else {
            // SAFE FALLBACK for generic Media types (e.g. YouTube Video if not defined as SocialPost)
            doc.append("type", "generic");
        }

        collection.insertOne(doc);
    }

    private boolean findByContent(String content) {
        return collection.find(new Document("content", content)).first() != null;
    }

    @Override
    public void updateSentiment(Media item, String sentiment) {
        collection.updateOne(
                new Document("content", item.getContent()).append("topic", item.getTopic()),
                new Document("$set", new Document("sentiment", sentiment))
        );
    }

    @Override
    public List<Media> findByTopic(String topic) {
        List<Media> items = new ArrayList<>();
        if (collection == null) return items;

        try {
            FindIterable<Document> docs = collection.find(new Document("topic", topic));
            for (Document doc : docs) {
                String type = doc.getString("type");
                if(type == null) type = "news";

                if ("news".equals(type)) {
                    items.add(new News(
                            doc.getString("topic"),
                            doc.getString("content"),
                            doc.getString("source"),
                            doc.getDate("timestamp"),
                            doc.getString("url"),
                            doc.getString("sentiment")
                    ));
                } else {
                    // Treat "social_post" and "generic" as SocialPost for now
                    items.add(new SocialPost(
                            doc.getString("topic"),
                            doc.getString("content"),
                            doc.getDate("timestamp"),
                            doc.getList("comments", String.class),
                            doc.getString("sentiment")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}