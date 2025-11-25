package project.app.humanelogistics.service;

public interface SentimentAnalyzer {
    enum SentimentType { POSITIVE, NEGATIVE, NEUTRAL }

    SentimentType analyze(String text);

    // New method to get a numerical value for mean calculation
    double analyzeScore(String text);
}