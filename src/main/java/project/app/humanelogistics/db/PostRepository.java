package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.SocialPost;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {
    private final MongoCollection<Document> collection;
    String connectionString = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    public PostRepository(String dbName, String collName) {
        try {
            MongoClient client = MongoClients.create(connectionString);
            MongoDatabase db = client.getDatabase(dbName);
            this.collection = db.getCollection(collName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

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
                        doc.getList("comments", String.class)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }
}