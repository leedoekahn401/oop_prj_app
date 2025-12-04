package project.app.humanelogistics.service;

public class DashboardStats {
    private final int totalPosts;
    private final double avgSentiment;
    private final String topDamageCategory;
    private final int topDamageCount;
    private final String aiSummary;

    public DashboardStats(int totalPosts, double avgSentiment,
                          String topDamageCategory, int topDamageCount,
                          String aiSummary) {
        this.totalPosts = totalPosts;
        this.avgSentiment = avgSentiment;
        this.topDamageCategory = topDamageCategory;
        this.topDamageCount = topDamageCount;
        this.aiSummary = aiSummary;
    }

    public int getTotalPosts() { return totalPosts; }
    public double getAvgSentiment() { return avgSentiment; }
    public String getTopDamageCategory() { return topDamageCategory; }
    public int getTopDamageCount() { return topDamageCount; }
    public String getAiSummary() { return aiSummary; }
}