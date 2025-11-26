package project.app.humanelogistics.service;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.preprocessing.DataCollector;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalysisService {

    private final MediaRepository repository;
    private final SentimentAnalyzer analyzer;
    private final List<DataCollector> collectors = new ArrayList<>();

    public AnalysisService(MediaRepository repository, SentimentAnalyzer analyzer) {
        this.repository = repository;
        this.analyzer = analyzer;
    }

    // === KEY CHANGE: Register new sources dynamically ===
    public void addCollector(DataCollector collector) {
        this.collectors.add(collector);
    }

    public void processNewData(String topic) {
        System.out.println("Starting Analysis Cycle for: " + topic);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(2);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");
        String sDate = startDate.format(fmt);
        String eDate = endDate.format(fmt);

        // Iterate through ALL registered collectors (Google, YouTube, etc.)
        for (DataCollector collector : collectors) {
            System.out.println("Fetching from source: " + collector.getClass().getSimpleName());
            List<Media> freshData = collector.collect(topic, sDate, eDate, 1);

            for(Media item : freshData) {
                repository.save(item);
                analyzeItemIfMissing(item);
            }
        }
        System.out.println("Analysis Cycle Complete.");
    }

    public void processMissingSentiments(String topic) {
        System.out.println("Scanning database for unscored items: " + topic);
        List<Media> posts = repository.findByTopic(topic);

        int updatedCount = 0;
        for (Media post : posts) {
            if (analyzeItemIfMissing(post)) {
                updatedCount++;
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        System.out.println("Database Grading Complete. Updated " + updatedCount + " items.");
    }

    private boolean analyzeItemIfMissing(Media item) {
        if(item.getSentiment() == null || item.getSentiment().isEmpty() || "null".equals(item.getSentiment())) {
            try {
                double score = analyzer.analyzeScore(item.getContent());
                String scoreStr = String.valueOf(score);
                item.setSentiment(scoreStr);
                repository.updateSentiment(item, scoreStr);
                System.out.println("   [GRADED] " + scoreStr + " | " + item.getContent());
                return true;
            } catch (Exception e) {
                System.err.println("Failed to grade item: " + e.getMessage());
            }
        }
        return false;
    }

    public int getTotalPostCount(String topic) {
        return repository.findByTopic(topic).size();
    }

    public double getOverallAverageScore(String topic) {
        List<Media> posts = repository.findByTopic(topic);
        if (posts.isEmpty()) return 0.0;
        double totalScore = 0.0;
        int count = 0;
        for (Media post : posts) {
            try {
                if (post.getSentiment() != null && !post.getSentiment().isEmpty()) {
                    totalScore += Double.parseDouble(post.getSentiment());
                    count++;
                }
            } catch (Exception e) { /* ignore */ }
        }
        return count == 0 ? 0.0 : totalScore / count;
    }

    public TimeSeriesCollection getSentimentData(String topic, int targetYear) {
        List<Media> posts = repository.findByTopic(topic);
        Map<LocalDate, Double> dailyTotalScore = new TreeMap<>();
        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        for (Media post : posts) {
            if (post.getTimestamp() == null) continue;
            LocalDate localDate = post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (targetYear != 0 && localDate.getYear() != targetYear) continue;

            double score = 0.0;
            try {
                if (post.getSentiment() != null) score = Double.parseDouble(post.getSentiment());
            } catch (Exception e) { score = 0.0; }

            dailyTotalScore.put(localDate, dailyTotalScore.getOrDefault(localDate, 0.0) + score);
            dailyCount.put(localDate, dailyCount.getOrDefault(localDate, 0) + 1);
        }

        TimeSeries averageSeries = new TimeSeries("Average Daily Sentiment");
        for (LocalDate date : dailyTotalScore.keySet()) {
            double avg = dailyTotalScore.get(date) / dailyCount.get(date);
            averageSeries.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), avg);
        }
        return new TimeSeriesCollection(averageSeries);
    }
}