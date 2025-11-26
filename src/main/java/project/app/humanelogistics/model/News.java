package project.app.humanelogistics.model;

import java.util.Date;

public class News extends Media {
    private String source; // e.g., "BBC", "CNN"
    private String url;

    public News(String topic, String title, String source, Date timestamp, String url, double sentiment) {
        // For news, the "content" usually starts with the title
        super(topic, title, timestamp, sentiment);
        this.source = source;
        this.url = url;
    }

    public String getSource() { return source; }
    public String getUrl() { return url; }
}