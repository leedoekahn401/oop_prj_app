package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.model.SocialPost;
import java.util.List;

public interface DataCollector {
    /**
     * Collects data based on a query and a date range.
     * @param query The search term (e.g., "Typhoon Yagi")
     * @param startDate Format: m/d/yyyy
     * @param endDate Format: m/d/yyyy
     * @param limit Number of pages or items to fetch
     * @return A list of collected Post objects
     */
    List<SocialPost> collect(String query, String startDate, String endDate, int limit);
}