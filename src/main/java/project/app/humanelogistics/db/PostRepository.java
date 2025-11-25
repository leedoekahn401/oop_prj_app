package project.app.humanelogistics.db;

import project.app.humanelogistics.model.SocialPost;
import java.util.List;

public interface PostRepository {
    void savePost(SocialPost post);
    void updateSentiment(SocialPost post, String sentiment); // New method
    List<SocialPost> findPostsByTopic(String topic);
}