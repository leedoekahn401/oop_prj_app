package project.app.humanelogistics.model;

import java.util.Date;

/**
 * Refactored News model.
 * Fixes Anemic Model by adding behavior (isReliableSource).
 */
public class News extends Media {
    private final String source;

    public News(String topic, String title, String source, String url, Date timestamp) {
        // We pass 'title' as content for simplicity, or we could have separate title/content fields
        super(topic, title, url, timestamp);
        this.source = source != null ? source : "Unknown Source";
    }

    public String getSource() { return source; }

    @Override
    public String getSourceLabel() {
        return "News: " + source;
    }

    // Domain Behavior (Fixing Anemic Model)
    public boolean isReliableSource() {
        // In a real app, this might come from a configuration
        return source.contains("BBC") || source.contains("CNN") || source.contains("Reuters");
    }
}