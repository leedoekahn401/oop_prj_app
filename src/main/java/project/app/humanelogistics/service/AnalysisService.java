package project.app.humanelogistics.service;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MongoPostRepository;
import project.app.humanelogistics.model.SocialPost;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalysisService {

    private final MongoPostRepository repository;
    private final SentimentAnalyzer analyzer;

    public AnalysisService(MongoPostRepository repository, SentimentAnalyzer analyzer) {
        this.repository = repository;
        this.analyzer = analyzer;
    }

    public TimeSeriesCollection getSentimentData(String topic, int targetYear) {
        List<SocialPost> posts = repository.findPostsByTopic(topic);
        Map<LocalDate, Double> dailyTotalScore = new TreeMap<>();
        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        for (SocialPost post : posts) {
            if (post.getTimestamp() == null) continue;

            LocalDate localDate = post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (localDate.getYear() != targetYear) continue;

            accumulateScore(post.getContent(), localDate, dailyTotalScore, dailyCount);
            if (post.getComments() != null) {
                for (String comment : post.getComments()) {
                    accumulateScore(comment, localDate, dailyTotalScore, dailyCount);
                }
            }
        }

        return convertToTimeSeries(dailyTotalScore, dailyCount);
    }

    private TimeSeriesCollection convertToTimeSeries(Map<LocalDate, Double> scores, Map<LocalDate, Integer> counts) {
        TimeSeries averageSeries = new TimeSeries("Average Daily Sentiment");
        for (LocalDate date : scores.keySet()) {
            double total = scores.get(date);
            int count = counts.get(date);
            averageSeries.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()),
                    (count == 0) ? 0 : total / count);
        }
        return new TimeSeriesCollection(averageSeries);
    }

    private void accumulateScore(String text, LocalDate localDate, Map<LocalDate, Double> scoreMap, Map<LocalDate, Integer> countMap) {
        double score = analyzer.analyzeScore(text);
        scoreMap.put(localDate, scoreMap.getOrDefault(localDate, 0.0) + score);
        countMap.put(localDate, countMap.getOrDefault(localDate, 0) + 1);
    }
}