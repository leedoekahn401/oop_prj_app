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
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    @Override
    public void save(Media item) {
        // 1. Save common fields (from Media parent)
        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("timestamp", item.getTimestamp())
                .append("sentiment", item.getSentiment());

        // 2. Save specific fields based on the actual object type (Polymorphism)
        if (item instanceof News) {
            News news = (News) item;
            doc.append("source", news.getSource());
            doc.append("url", news.getUrl());
            doc.append("type", "news");
        } else if (item instanceof SocialPost) {
            SocialPost post = (SocialPost) item;
            doc.append("comments", post.getComments());
            doc.append("type", "social_post");
        }

        collection.insertOne(doc);
    }

    @Override
    public void updateSentiment(Media item, String sentiment) {
        // Find by content + topic to update the sentiment field
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

                if ("news".equals(type)) {
                    // Reconstruct News object
                    items.add(new News(
                            doc.getString("topic"),
                            doc.getString("content"),
                            doc.getString("source"),
                            doc.getDate("timestamp"),
                            doc.getString("url"),
                            doc.getString("sentiment")
                    ));
                } else {
                    // Reconstruct SocialPost object
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

    // Optional: Delete items with null sentiment
    public void deleteItemsWithNullSentiment() {
        collection.deleteMany(new Document("sentiment", null));
    }
}