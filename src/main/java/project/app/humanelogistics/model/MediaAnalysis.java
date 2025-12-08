package project.app.humanelogistics.model;

import java.util.Objects;

/**
 * NEW CLASS: MediaAnalysis
 * * Responsibility: Links a specific Media item with its analysis results.
 * Pattern: Immutable wrapper. Any change in analysis results in a new instance.
 */
public class MediaAnalysis {
    private final Media media;
    private final SentimentScore sentiment;
    private final DamageCategory damageCategory;

    public MediaAnalysis(Media media, SentimentScore sentiment, DamageCategory damageCategory) {
        this.media = Objects.requireNonNull(media, "Media cannot be null");
        this.sentiment = sentiment != null ? sentiment : SentimentScore.neutral();
        this.damageCategory = damageCategory != null ? damageCategory : DamageCategory.UNKNOWN;
    }

    // Factory for initial creation (no analysis yet)
    public static MediaAnalysis unprocessed(Media media) {
        return new MediaAnalysis(media, SentimentScore.neutral(), DamageCategory.UNKNOWN);
    }

    // --- Accessors ---
    public Media getMedia() { return media; }
    public SentimentScore getSentiment() { return sentiment; }
    public DamageCategory getDamageCategory() { return damageCategory; }

    // --- Immutability Helpers (Wither pattern) ---

    public MediaAnalysis withSentiment(SentimentScore newSentiment) {
        return new MediaAnalysis(this.media, newSentiment, this.damageCategory);
    }

    public MediaAnalysis withDamageCategory(DamageCategory newCategory) {
        return new MediaAnalysis(this.media, this.sentiment, newCategory);
    }
}