package project.app.humanelogistics.service;

public interface SummaryGenerator {
        String generateSummary(String topic, int postCount, double avgSentiment, String topDamageType);
}