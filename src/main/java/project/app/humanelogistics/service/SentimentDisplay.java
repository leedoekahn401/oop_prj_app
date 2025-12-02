package project.app.humanelogistics.service;

import javafx.scene.control.Label;
import project.app.humanelogistics.model.SentimentScore;

/**
 * "Tell, Don't Ask" Helper.
 * Encapsulates the logic of how a SentimentScore should be displayed in the UI.
 * The Controller should just tell this class to "apply" the style.
 */
public class SentimentDisplay {
    private final SentimentScore score;

    public SentimentDisplay(SentimentScore score) {
        this.score = score;
    }

    public void applyTo(Label label) {
        if (label == null) return;

        label.setText(getText());
        label.setStyle(getStyle());
    }

    private String getText() {
        if (score.isPositive()) return "Positive";
        if (score.isNegative()) return "Negative";
        return "Neutral";
    }

    private String getStyle() {
        if (score.isPositive()) return "-fx-text-fill: #27AE60; -fx-font-weight: bold;";
        if (score.isNegative()) return "-fx-text-fill: #C0392B; -fx-font-weight: bold;";
        return "-fx-text-fill: #7F8C8D; -fx-font-weight: bold;";
    }
}