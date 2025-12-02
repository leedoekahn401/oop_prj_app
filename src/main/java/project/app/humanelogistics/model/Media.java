package project.app.humanelogistics.model;

import java.time.Instant;
import java.util.*;

/**
 * Abstract Base Class.
 * Refactored for:
 * 1. Open/Closed Principle (via analysisResults map)
 * 2. Immutability (final fields)
 * 3. Encapsulation (protected constructors, specific accessors)
 */
public abstract class Media {
    // Core Identity - Immutable
    private final String topic;
    private final String content;
    private final String url;
    private final Date timestamp;

    // Extensible Analysis Data (OCP)
    // Maps a key (e.g., "sentiment") to a value (e.g., SentimentScore object)
    protected final Map<String, Object> analysisResults = new HashMap<>();

    protected Media(String topic, String content, String url, Date timestamp) {
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.url = url != null ? url : "";
        this.timestamp = timestamp != null ? timestamp : new Date();
    }

    // --- Getters ---
    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public String getUrl() { return url; }
    public Date getTimestamp() { return new Date(timestamp.getTime()); } // Defensive copy

    // --- OCP Analysis Handling ---

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

    // Helper wrappers for common analysis to maintain developer ergonomics
    public SentimentScore getSentiment() {
        return getAnalysisResult("sentiment", SentimentScore.class)
                .orElse(SentimentScore.neutral());
    }

    public DamageCategory getDamageCategory() {
        return getAnalysisResult("damageCategory", DamageCategory.class)
                .orElse(DamageCategory.UNKNOWN);
    }

    // Abstract behavior method
    public abstract String getSourceLabel();
}