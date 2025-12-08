package project.app.humanelogistics.db;

import project.app.humanelogistics.model.MediaAnalysis;
import java.util.List;

public interface MediaRepository {
    void save(MediaAnalysis analysis);

    void updateAnalysis(MediaAnalysis analysis);

    List<MediaAnalysis> findByTopic(String topic);
}