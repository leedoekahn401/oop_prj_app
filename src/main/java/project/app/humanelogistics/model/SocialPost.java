package project.app.humanelogistics.model;

import java.util.Date;
import java.util.List;

public class SocialPost {
    private String topic;
    private String content;
    private Date timestamp;
    private List<String> comments;

    public SocialPost(String topic, String content, Date timestamp, List<String> comments) {
        this.topic = topic;
        this.content = content;
        this.timestamp = timestamp;
        this.comments = comments;
    }

    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public List<String> getComments() { return comments; }
}