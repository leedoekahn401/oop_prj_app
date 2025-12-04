package project.app.humanelogistics.service;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeriesCollection;

public class DashboardService {
    private final AnalysisService analysisService;
    private final String defaultTopic;
    private final int defaultYear;

    public DashboardService(AnalysisService analysisService, String defaultTopic, int defaultYear) {
        this.analysisService = analysisService;
        this.defaultTopic = defaultTopic;
        this.defaultYear = defaultYear;
    }

    /**
     * Retrieves all dashboard statistics in one call
     * Fixes: Business logic extracted from controller
     */
    public DashboardStats getDashboardStats() {
        return getDashboardStats(defaultTopic);
    }

    public DashboardStats getDashboardStats(String topic) {
        int totalPosts = analysisService.getTotalPostCount(topic);
        double avgSentiment = analysisService.getOverallAverageScore(topic);

        // FIXED: Extract this calculation from controller
        DefaultCategoryDataset damageData = analysisService.getDamageData(topic);
        DamageInfo damageInfo = calculateTopDamage(damageData);

        String summary = analysisService.generateTopicInsight(topic);

        return new DashboardStats(
                totalPosts,
                avgSentiment,
                damageInfo.getTopCategory(),
                damageInfo.getTopCount(),
                summary
        );
    }

    /**
     * FIXED: Business logic moved from controller to service
     */
    private DamageInfo calculateTopDamage(DefaultCategoryDataset dataset) {
        String topCategory = "None";
        int maxCount = 0;

        if (dataset != null && dataset.getColumnCount() > 0) {
            for (int i = 0; i < dataset.getColumnCount(); i++) {
                Number value = dataset.getValue(0, i);
                if (value != null && value.intValue() > maxCount) {
                    maxCount = value.intValue();
                    Comparable key = dataset.getColumnKey(i);
                    topCategory = (key != null) ? key.toString() : "Unknown";
                }
            }
        }

        return new DamageInfo(topCategory, maxCount);
    }

    public TimeSeriesCollection getSentimentTimeSeries() {
        return getSentimentTimeSeries(defaultTopic);
    }

    public TimeSeriesCollection getSentimentTimeSeries(String topic) {
        return analysisService.getSentimentData(topic, defaultYear);
    }

    public DefaultCategoryDataset getDamageDataset() {
        return getDamageDataset(defaultTopic);
    }

    public DefaultCategoryDataset getDamageDataset(String topic) {
        return analysisService.getDamageData(topic);
    }

    // Inner class for damage information
    public static class DamageInfo {
        private final String topCategory;
        private final int topCount;

        public DamageInfo(String topCategory, int topCount) {
            this.topCategory = topCategory;
            this.topCount = topCount;
        }

        public String getTopCategory() { return topCategory; }
        public int getTopCount() { return topCount; }
    }
}