package project.app.humanelogistics.model;

import java.util.Objects;

public class MediaAnalysis {
    private final Media media;
    private final SentimentScore sentiment;
    private final DamageCategory damageCategory;

    public MediaAnalysis(Media media, SentimentScore sentiment, DamageCategory damageCategory) {
        this.media = Objects.requireNonNull(media, "Media cannot be null");
        this.sentiment = sentiment != null ? sentiment : SentimentScore.neutral();
        this.damageCategory = damageCategory != null ? damageCategory : DamageCategory.UNKNOWN;
    }

    public static MediaAnalysis unprocessed(Media media) {
        return new MediaAnalysis(media, SentimentScore.neutral(), DamageCategory.UNKNOWN);
    }

    public Media getMedia() { return media; }
    public SentimentScore getSentiment() { return sentiment; }
    public DamageCategory getDamageCategory() { return damageCategory; }


    public MediaAnalysis withSentiment(SentimentScore newSentiment) {
        return new MediaAnalysis(this.media, newSentiment, this.damageCategory);
    }

    public MediaAnalysis withDamageCategory(DamageCategory newCategory) {
        return new MediaAnalysis(this.media, this.sentiment, newCategory);
    }
}