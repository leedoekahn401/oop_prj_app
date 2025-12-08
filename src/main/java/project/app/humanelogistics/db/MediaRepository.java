package project.app.humanelogistics.db;

import project.app.humanelogistics.model.Media;
import java.util.List;

public interface MediaRepository {
    void save(Media item);

    void updateSentiment(Media item, Double sentiment);
    void updateAnalysis(Media item);

    List<Media> findByTopic(String topic);
}