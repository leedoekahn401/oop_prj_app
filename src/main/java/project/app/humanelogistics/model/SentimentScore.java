package project.app.humanelogistics.model;

/**
 * Value Object representing a sentiment score.
 * Fixes Primitive Obsession by encapsulating the double value and its validation logic.
 */
public class SentimentScore {
    private final double value;

    private SentimentScore(double value) {
        if (value < -1.0) value = -1.0;
        if (value > 1.0) value = 1.0;
        this.value = value;
    }

    public static SentimentScore of(double value) {
        return new SentimentScore(value);
    }

    public static SentimentScore neutral() {
        return new SentimentScore(0.0);
    }

    public double getValue() {
        return value;
    }

    public boolean isPositive() { return value > 0.1; }
    public boolean isNegative() { return value < -0.1; }
    public boolean isNeutral() { return !isPositive() && !isNegative(); }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}