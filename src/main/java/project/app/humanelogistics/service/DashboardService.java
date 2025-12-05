package project.app.humanelogistics.service;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeriesCollection;

public class DashboardService {
    // CHANGED: Depends on StatisticsService now
    private final StatisticsService statisticsService;
    private final String defaultTopic;
    private final int defaultYear;

    public DashboardService(StatisticsService statisticsService, String defaultTopic, int defaultYear) {
        this.statisticsService = statisticsService;
        this.defaultTopic = defaultTopic;
        this.defaultYear = defaultYear;
    }

    public DashboardStats getDashboardStats() {
        return getDashboardStats(defaultTopic);
    }

    public DashboardStats getDashboardStats(String topic) {
        int totalPosts = statisticsService.getTotalPostCount(topic);
        double avgSentiment = statisticsService.getOverallAverageScore(topic);

        // This logic is now inside StatisticsService
        String topCategory = statisticsService.getTopDamageCategory(topic);

        // Use a simpler approach to get the count for the top category
        DefaultCategoryDataset damageData = statisticsService.getDamageData(topic);
        int topCount = getCountForCategory(damageData, topCategory);

        String summary = statisticsService.generateTopicInsight(topic);

        return new DashboardStats(
                totalPosts,
                avgSentiment,
                topCategory,
                topCount,
                summary
        );
    }

    private int getCountForCategory(DefaultCategoryDataset dataset, String category) {
        if (dataset == null || category.equals("None")) return 0;
        try {
            Number value = dataset.getValue("Damage Reports", category);
            return value != null ? value.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public TimeSeriesCollection getSentimentTimeSeries() {
        return statisticsService.getSentimentData(defaultTopic, defaultYear);
    }

    public DefaultCategoryDataset getDamageDataset() {
        return statisticsService.getDamageData(defaultTopic);
    }
}