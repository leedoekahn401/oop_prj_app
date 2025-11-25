package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.SocialPost;

import java.util.ArrayList;
import java.util.List;

public class MongoPostRepository implements PostRepository {
    private final MongoCollection<Document> collection;

    // FIXED: Constructor now accepts 'connectionString' as the first argument
    public MongoPostRepository(String connectionString, String dbName, String collName) {
        try {
            MongoClient client = MongoClients.create(connectionString);
            MongoDatabase db = client.getDatabase(dbName);
            this.collection = db.getCollection(collName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    @Override
    public void savePost(SocialPost post) {
        Document doc = new Document("topic", post.getTopic())
                .append("content", post.getContent())
                .append("timestamp", post.getTimestamp())
                .append("comments", post.getComments())
                .append("sentiment", post.getSentiment());
        collection.insertOne(doc);
    }

    @Override
    public void updateSentiment(SocialPost post, String sentiment) {
        // Find by content + topic to ensure we update the correct post
        collection.updateOne(
                new Document("content", post.getContent()).append("topic", post.getTopic()),
                new Document("$set", new Document("sentiment", sentiment))
        );
    }

    @Override
    public List<SocialPost> findPostsByTopic(String topic) {
        List<SocialPost> posts = new ArrayList<>();
        if (collection == null) return posts;

        try {
            FindIterable<Document> docs = collection.find(new Document("topic", topic));
            for (Document doc : docs) {
                posts.add(new SocialPost(
                        doc.getString("topic"),
                        doc.getString("content"),
                        doc.getDate("timestamp"),
                        doc.getList("comments", String.class),
                        doc.getString("sentiment")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }
}