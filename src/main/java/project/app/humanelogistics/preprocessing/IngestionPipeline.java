package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.preprocessing.analysis.ContentClassifier;
import project.app.humanelogistics.preprocessing.analysis.SentimentAnalyzer;
import project.app.humanelogistics.preprocessing.analysis.WebContentFetcher;
import project.app.humanelogistics.preprocessing.collector.DataCollector;

import java.util.*;

/**
 * Refactored AnalysisService.
 * Responsibility: Coordinating data ingestion and running analysis algorithms (Sentiment/Classification).
 * No longer responsible for Statistics or Reporting.
 */
public class IngestionPipeline {

    private final Map<String, MediaRepository> repoMap = new LinkedHashMap<>();
    private final SentimentAnalyzer sentimentAnalyzer;
    private final ContentClassifier damageClassifier;
    private final WebContentFetcher contentFetcher;
    private final List<DataCollector> collectors = new ArrayList<>();

    public IngestionPipeline(SentimentAnalyzer sentimentAnalyzer, ContentClassifier damageClassifier) {
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.damageClassifier = damageClassifier;
        this.contentFetcher = new WebContentFetcher();
    }

    public void addRepository(String label, MediaRepository repo) {
        this.repoMap.put(label, repo);
    }

    public void registerCollectors(DataCollector... newCollectors) {
        Collections.addAll(this.collectors, newCollectors);
    }

    public void clearCollectors() {
        this.collectors.clear();
    }

    public void processNewData(String topic, String startDate, String endDate, boolean analyzeImmediately) {
        System.out.println("Starting Cycle for: " + topic + " [" + startDate + " to " + endDate + "]");

        for (DataCollector collector : collectors) {
            List<Media> freshData = collector.collect(topic, startDate, endDate, 1);
            for(Media item : freshData) {
                if (analyzeImmediately) {
                    analyzeItem(item);
                }
                if (!repoMap.isEmpty()) {
                    repoMap.values().iterator().next().save(item);
                }
            }
        }
    }

    public void processExistingData(String topic) {
        System.out.println("Scanning database for un-analyzed items: " + topic);
        int count = 0;

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            System.out.println("Found " + posts.size() + " items in repo. Checking for missing analysis...");

            for (Media item : posts) {
                boolean needsAnalysis = (item.getSentiment().getValue() == 0.0) ||
                        (item.getDamageCategory() == DamageCategory.UNKNOWN);

                if (needsAnalysis) {
                    System.out.println(" -> Analyzing: " + truncate(item.getContent()) + "...");
                    analyzeItem(item);
                    repo.updateAnalysis(item);
                    count++;

                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
        }
        System.out.println("Batch Analysis Complete. Updated " + count + " items.");
    }

    private void analyzeItem(Media item) {
        String textToAnalyze = item.getContent();

        // 1. Fetch full content if URL exists
        String url = item.getUrl();
        if (url != null && !url.isEmpty() && url.startsWith("http")) {
            System.out.print("   [FETCHING] Reading article... ");
            String fullBody = contentFetcher.fetchUrlContent(url);

            if (!fullBody.isEmpty()) {
                textToAnalyze = fullBody;
                System.out.println("Success (" + fullBody.length() + " chars)");
            } else {
                System.out.println("Failed/Skipped");
            }
        }

        // 2. Run Sentiment Analysis
        try {
            double score = sentimentAnalyzer.analyzeScore(textToAnalyze);
            item.addAnalysisResult("sentiment", SentimentScore.of(score));
        } catch (Exception e) {
            System.err.println("Sentiment Error: " + e.getMessage());
        }

        // 3. Run Damage Classification
        try {
            if (damageClassifier != null) {
                DamageCategory cat = damageClassifier.classify(textToAnalyze);
                item.addAnalysisResult("damageCategory", cat);
                System.out.println("   [RESULT] Score: " + item.getSentiment().getValue() + " | Type: " + cat);
            }
        } catch (Exception e) {
            System.err.println("Classification Error: " + e.getMessage());
        }
    }

    private String truncate(String input) {
        if (input == null) return "";
        return input.length() > 50 ? input.substring(0, 50) : input;
    }
}