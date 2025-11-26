package project.app.humanelogistics.model;

import java.util.Date;
import java.util.List;

public class SocialPost extends Media{
    private List<String> comments;

    public SocialPost(String topic, String content, Date timestamp, List<String> comments, double sentiment) {
        super(topic, content, timestamp, sentiment);
        this.comments = comments;
    }

    // Overloaded constructor for backward compatibility if needed
    public SocialPost(String topic, String content, Date timestamp, List<String> comments) {
        this(topic, content, timestamp, comments, 0.0);
    }

    public List<String> getComments() { return comments; }
}