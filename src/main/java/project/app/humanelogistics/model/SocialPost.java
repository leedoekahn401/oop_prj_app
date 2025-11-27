package project.app.humanelogistics.model;

import java.util.Date;
import java.util.List;

public class SocialPost extends Media {
    private List<String> comments;

    // THIS IS THE CONSTRUCTOR YOUR REPOSITORY IS TRYING TO CALL (6 args)
    public SocialPost(String topic, String content, Date timestamp, List<String> comments, Double sentiment, String url) {
        // Pass url to parent constructor
        super(topic, content, timestamp, sentiment, url);
        this.comments = comments;
    }

    // Backward compatibility constructor (5 args)
    public SocialPost(String topic, String content, Date timestamp, List<String> comments, Double sentiment) {
        this(topic, content, timestamp, comments, sentiment, null);
    }

    public List<String> getComments() { return comments; }
}