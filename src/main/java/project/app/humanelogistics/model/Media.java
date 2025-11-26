package project.app.humanelogistics.model;

import java.util.Date;

public abstract class Media {
    protected String topic;
    protected String content;
    protected Date timestamp;
    protected double sentiment;

    public Media(String topic, String content, Date timestamp, double sentiment) {
        this.topic = topic;
        this.content = content;
        this.timestamp = timestamp;
        this.sentiment = sentiment;
    }

    // Common Getters and Setters
    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public double getSentiment() { return sentiment; }
    public void setSentiment(double sentiment) { this.sentiment = sentiment; }
}