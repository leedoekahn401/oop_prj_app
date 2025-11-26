package project.app.humanelogistics.db;

import project.app.humanelogistics.model.Media;
import java.util.List;

public interface MediaRepository {
    void save(Media item);
    // CHANGED: String -> Double to match implementation
    void updateSentiment(Media item, Double sentiment);
    List<Media> findByTopic(String topic);
}