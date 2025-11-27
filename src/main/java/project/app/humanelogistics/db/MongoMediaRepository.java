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
        if(findByContent(item.getContent())) return;

        // Save common fields including URL
        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("timestamp", item.getTimestamp())
                .append("sentiment", item.getSentiment())
                .append("url", item.getUrl()); // Common field

        if (item instanceof News) {
            News news = (News) item;
            doc.append("source", news.getSource());
            // url is already added above
            doc.append("type", "news");
        } else if (item instanceof SocialPost) {
            SocialPost post = (SocialPost) item;
            doc.append("comments", post.getComments());
            // url is already added above
            doc.append("type", "social_post");
        } else {
            doc.append("type", "generic");
        }

        collection.insertOne(doc);
    }

    private boolean findByContent(String content) {
        return collection.find(new Document("content", content)).first() != null;
    }

    @Override
    public void updateSentiment(Media item, Double sentiment) {
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

                Double sentiment = null;
                try {
                    sentiment = doc.get("sentiment", Double.class);
                } catch (Exception e) {
                    try {
                        String s = doc.getString("sentiment");
                        if (s != null) sentiment = Double.parseDouble(s);
                    } catch (Exception ignored) {}
                }

                // Retrieve common URL
                String url = doc.getString("url");

                if ("news".equals(type)) {
                    items.add(new News(
                            doc.getString("topic"),
                            doc.getString("content"),
                            doc.getString("source"),
                            doc.getDate("timestamp"),
                            url, // Pass URL
                            sentiment
                    ));
                } else {
                    items.add(new SocialPost(
                            doc.getString("topic"),
                            doc.getString("content"),
                            doc.getDate("timestamp"),
                            doc.getList("comments", String.class),
                            sentiment,
                            url // Pass URL
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}