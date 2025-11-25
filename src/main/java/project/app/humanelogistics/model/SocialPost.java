package project.app.humanelogistics.model;

import java.util.Date;
import java.util.List;

public class SocialPost {
    private String topic;
    private String content;
    private Date timestamp;
    private List<String> comments;
    private String sentiment; // Added for caching API results later

    public SocialPost(String topic, String content, Date timestamp, List<String> comments, String sentiment) {
        this.topic = topic;
        this.content = content;
        this.timestamp = timestamp;
        this.comments = comments;
        this.sentiment = sentiment;
    }

    // Overloaded constructor for backward compatibility
    public SocialPost(String topic, String content, Date timestamp, List<String> comments) {
        this(topic, content, timestamp, comments, null);
    }

    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public List<String> getComments() { return comments; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
}