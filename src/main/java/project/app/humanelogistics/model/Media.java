package project.app.humanelogistics.model;

import java.util.Date;

public abstract class Media {
    protected String topic;
    protected String content;
    protected Date timestamp;
    protected Double sentiment;
    protected String url; // MOVED: URL is now a common field for all media

    public Media(String topic, String content, Date timestamp, Double sentiment, String url) {
        this.topic = topic;
        this.content = content;
        this.timestamp = timestamp;
        this.sentiment = sentiment;
        this.url = url;
    }

    // Backward compatibility constructor (optional)
    public Media(String topic, String content, Date timestamp, Double sentiment) {
        this(topic, content, timestamp, sentiment, null);
    }

    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public Double getSentiment() { return sentiment; }
    public void setSentiment(Double sentiment) { this.sentiment = sentiment; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}