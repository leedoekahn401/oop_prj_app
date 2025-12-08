package project.app.humanelogistics.model;

import java.util.Date;
import java.util.Objects;

/**
 * Refactored Abstract Base Class.
 * * CHANGES:
 * - Removed all analysis/mutable logic.
 * - This class is now purely an immutable data carrier for the core content.
 */
public abstract class Media {
    private final String topic;
    private final String content;
    private final String url;
    private final Date timestamp;

    protected Media(String topic, String content, String url, Date timestamp) {
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.url = url != null ? url : "";
        this.timestamp = timestamp != null ? new Date(timestamp.getTime()) : new Date();
    }

    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public String getUrl() { return url; }
    public Date getTimestamp() { return new Date(timestamp.getTime()); }

    public abstract String getSourceLabel();
}