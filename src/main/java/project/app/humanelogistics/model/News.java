package project.app.humanelogistics.model;

import java.util.Date;


public class News extends Media {
    private final String source;

    public News(String topic, String title, String source, String url, Date timestamp) {
        super(topic, title, url, timestamp);
        this.source = source != null ? source : "Unknown Source";
    }

    public String getSource() { return source; }

    @Override
    public String getSourceLabel() {
        return "News: " + source;
    }
}