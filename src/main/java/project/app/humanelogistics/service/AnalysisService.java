package project.app.humanelogistics.service;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.Media;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalysisService {

    private final MediaRepository repository;
    private final SentimentAnalyzer analyzer;

    public AnalysisService(MediaRepository repository, SentimentAnalyzer analyzer) {
        this.repository = repository;
        this.analyzer = analyzer;
    }

    /**
     * PASSIVE MODE: Does not call API.
     * Prevents UI freezing. Analysis is done by running SentimentGrade.main() separately.
     */
    public void processAndCacheSentiment(String topic) {
        // No-op
    }

    // === NEW METHODS FOR DASHBOARD ===

    public int getTotalPostCount(String topic) {
        // Simply counts items in the list
        return repository.findByTopic(topic).size();
    }

    public double getOverallAverageScore(String topic) {
        List<Media> posts = repository.findByTopic(topic);
        if (posts.isEmpty()) return 0.0;

        double totalScore = 0.0;
        int count = 0;

        for (Media post : posts) {
            try {
                // Only count posts that have been analyzed (have a valid number)
                if (post.getSentiment() != null && !post.getSentiment().isEmpty()) {
                    double s = Double.parseDouble(post.getSentiment());
                    totalScore += s;
                    count++;
                }
            } catch (Exception e) {
                // Ignore parsing errors or old data formats
            }
        }
        return count == 0 ? 0.0 : totalScore / count;
    }
    // =================================

    public TimeSeriesCollection getSentimentData(String topic, int targetYear) {
        List<Media> posts = repository.findByTopic(topic);

        Map<LocalDate, Double> dailyTotalScore = new TreeMap<>();
        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        for (Media post : posts) {
            if (post.getTimestamp() == null) continue;

            LocalDate localDate = post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (localDate.getYear() != targetYear) continue;

            double score = 0.0;
            try {
                // Pure Retrieval: Read from DB
                if (post.getSentiment() != null) {
                    score = Double.parseDouble(post.getSentiment());
                }
            } catch (Exception e) {
                score = 0.0;
            }

            dailyTotalScore.put(localDate, dailyTotalScore.getOrDefault(localDate, 0.0) + score);
            dailyCount.put(localDate, dailyCount.getOrDefault(localDate, 0) + 1);
        }

        return createAverageSeries(dailyTotalScore, dailyCount);
    }

    private TimeSeriesCollection createAverageSeries(Map<LocalDate, Double> totals, Map<LocalDate, Integer> counts) {
        TimeSeries averageSeries = new TimeSeries("Average Daily Sentiment");

        for (LocalDate date : totals.keySet()) {
            double total = totals.get(date);
            int count = counts.get(date);
            double avg = (count == 0) ? 0 : total / count;

            averageSeries.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), avg);
        }

        return new TimeSeriesCollection(averageSeries);
    }
}