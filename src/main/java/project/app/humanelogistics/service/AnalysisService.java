package project.app.humanelogistics.service;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.preprocessing.DataCollector;
import project.app.humanelogistics.preprocessing.GoogleNewsCollector;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AnalysisService {

    // Map<RepositoryLabel, RepositoryObject>
    // e.g. "News" -> MongoMediaRepository(news)
    //      "Social Posts" -> MongoMediaRepository(posts)
    private final Map<String, MediaRepository> repoMap = new LinkedHashMap<>();

    private final SentimentAnalyzer analyzer;
    private final List<DataCollector> collectors = new ArrayList<>();

    public AnalysisService(SentimentAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void addRepository(String label, MediaRepository repo) {
        this.repoMap.put(label, repo);
    }

    public void registerCollectors(DataCollector... newCollectors) {
        for (DataCollector collector : newCollectors) {
            this.collectors.add(collector);
        }
    }

    public void clearCollectors() {
        this.collectors.clear();
    }

    public void registerDefaultCollectors() {
        registerCollectors(new GoogleNewsCollector());
    }

    public void processNewData(String topic) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(2);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");
        processNewData(topic, startDate.format(fmt), endDate.format(fmt), true);
    }

    public void processNewData(String topic, String startDate, String endDate) {
        processNewData(topic, startDate, endDate, true);
    }

    public void processNewData(String topic, String startDate, String endDate, boolean gradeImmediately) {
        System.out.println("Starting Cycle for: " + topic + " [" + startDate + " to " + endDate + "]");
        if (!gradeImmediately) System.out.println("Mode: Search Only (Grading Skipped)");

        for (DataCollector collector : collectors) {
            System.out.println("Fetching from source: " + collector.getClass().getSimpleName());
            List<Media> freshData = collector.collect(topic, startDate, endDate, 1);
            for(Media item : freshData) {
                // Save to the first registered repository by default (usually "News")
                if (!repoMap.isEmpty()) {
                    repoMap.values().iterator().next().save(item);
                }

                if (gradeImmediately) analyzeItemIfMissing(item);
            }
        }
    }

    public void processMissingSentiments(String topic) {
        System.out.println("Scanning all databases for unscored items: " + topic);
        int updatedCount = 0;

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            for (Media post : posts) {
                if (analyzeItemIfMissing(post, repo)) updatedCount++;
            }
        }
        System.out.println("Database Grading Complete. Updated " + updatedCount + " items.");
    }

    private boolean analyzeItemIfMissing(Media item) {
        if (!repoMap.isEmpty()) {
            return analyzeItemIfMissing(item, repoMap.values().iterator().next());
        }
        return false;
    }

    private boolean analyzeItemIfMissing(Media item, MediaRepository repo) {
        if(isSentimentMissing(item)) {
            try {
                double score = analyzer.analyzeScore(item.getContent());
                item.setSentiment(score);
                repo.updateSentiment(item, score);
                System.out.println("   [GRADED] " + score + " | " + item.getContent());
                return true;
            } catch (Exception e) {
                System.err.println("Failed to grade item: " + e.getMessage());
            }
        }
        return false;
    }

    public int getTotalPostCount(String topic) {
        int total = 0;
        for (MediaRepository repo : repoMap.values()) {
            total += repo.findByTopic(topic).size();
        }
        return total;
    }

    public double getOverallAverageScore(String topic) {
        double totalScore = 0.0;
        int count = 0;

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            for (Media post : posts) {
                if (!isSentimentMissing(post)) {
                    totalScore += post.getSentiment();
                    count++;
                }
            }
        }
        return count == 0 ? 0.0 : totalScore / count;
    }

    // FIX: Generate separate series for each repository in repoMap
    public TimeSeriesCollection getSentimentData(String topic, int targetYear) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        // Loop through "News", "Social Posts", etc.
        for (Map.Entry<String, MediaRepository> entry : repoMap.entrySet()) {
            String seriesName = entry.getKey();
            MediaRepository repo = entry.getValue();

            List<Media> posts = repo.findByTopic(topic);

            // Map<Date, TotalScore>
            Map<LocalDate, Double> dailyTotal = new TreeMap<>();
            Map<LocalDate, Integer> dailyCount = new HashMap<>();

            boolean hasData = false;

            for (Media post : posts) {
                if (post.getTimestamp() == null) continue;
                if (isSentimentMissing(post)) continue;

                LocalDate localDate = post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (targetYear != 0 && localDate.getYear() != targetYear) continue;

                double score = post.getSentiment();
                dailyTotal.put(localDate, dailyTotal.getOrDefault(localDate, 0.0) + score);
                dailyCount.put(localDate, dailyCount.getOrDefault(localDate, 0) + 1);
                hasData = true;
            }

            // Create series for this repo
            TimeSeries series = new TimeSeries(seriesName);
            for (LocalDate date : dailyTotal.keySet()) {
                double avg = dailyTotal.get(date) / dailyCount.get(date);
                series.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), avg);
            }

            if (hasData) {
                dataset.addSeries(series);
            } else {
                System.out.println("Warning: No valid sentiment data found for '" + seriesName + "'");
            }
        }

        return dataset;
    }

    private boolean isSentimentMissing(Media item) {
        return item.getSentiment() == 0.0;
    }
}