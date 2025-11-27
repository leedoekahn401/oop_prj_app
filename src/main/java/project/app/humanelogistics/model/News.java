package project.app.humanelogistics.model;

import java.util.Date;

public class News extends Media {
    private String source;
    // URL removed from here, inherited from Media

    public News(String topic, String title, String source, Date timestamp, String url, Double sentiment) {

        super(topic, title, timestamp, sentiment, url);
        this.source = source;
    }

    public String getSource() { return source; }
}