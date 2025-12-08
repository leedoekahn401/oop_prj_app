package project.app.humanelogistics.preprocessing.collector;

import project.app.humanelogistics.model.Media;
import java.util.List;

public interface DataCollector {
    List<Media> collect(String query, String startDate, String endDate, int limit);
}