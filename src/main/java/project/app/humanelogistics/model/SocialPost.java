package project.app.humanelogistics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class SocialPost extends Media {
    private final List<String> comments;

    public SocialPost(String topic, String content, String url, Date timestamp, List<String> comments) {
        super(topic, content, url, timestamp);
        this.comments = comments != null ? new ArrayList<>(comments) : new ArrayList<>();
    }

    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public String getSourceLabel() {
        return "Social Media";
    }

}