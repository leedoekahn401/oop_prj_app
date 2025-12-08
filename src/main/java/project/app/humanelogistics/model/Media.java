package project.app.humanelogistics.model;

import java.time.Instant;
import java.util.*;


public abstract class Media {
    private final String topic;
    private final String content;
    private final String url;
    private final Date timestamp;

    protected final Map<String, Object> analysisResults = new HashMap<>();

    protected Media(String topic, String content, String url, Date timestamp) {
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.url = url != null ? url : "";
        this.timestamp = timestamp != null ? timestamp : new Date();
    }

    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public String getUrl() { return url; }
    public Date getTimestamp() { return new Date(timestamp.getTime()); } // Defensive copy

    public void addAnalysisResult(String key, Object result) {
        this.analysisResults.put(key, result);
    }

    public <T> Optional<T> getAnalysisResult(String key, Class<T> type) {
        Object result = analysisResults.get(key);
        if (type.isInstance(result)) {
            return Optional.of(type.cast(result));
        }
        return Optional.empty();
    }

    public SentimentScore getSentiment() {
        return getAnalysisResult("sentiment", SentimentScore.class)
                .orElse(SentimentScore.neutral());
    }

    public DamageCategory getDamageCategory() {
        return getAnalysisResult("damageCategory", DamageCategory.class)
                .orElse(DamageCategory.UNKNOWN);
    }

    public abstract String getSourceLabel();
}