package project.app.humanelogistics.db;

import project.app.humanelogistics.model.Media;
import java.util.List;

public interface MediaRepository {
    void save(Media item);
    void updateSentiment(Media item, String sentiment);

    // FIX: Renamed to match the implementation in MongoMediaRepository
    List<Media> findByTopic(String topic);
}