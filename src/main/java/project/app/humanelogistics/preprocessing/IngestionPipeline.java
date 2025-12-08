package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.MediaAnalysis;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.preprocessing.analysis.ContentClassifier;
import project.app.humanelogistics.preprocessing.analysis.SentimentAnalyzer;
import project.app.humanelogistics.preprocessing.analysis.WebContentFetcher;
import project.app.humanelogistics.preprocessing.collector.DataCollector;

import java.util.*;

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
            // Collector returns raw Media
            List<Media> freshData = collector.collect(topic, startDate, endDate, 1);

            for(Media item : freshData) {
                // Wrap raw Media in Analysis object
                MediaAnalysis analysis = MediaAnalysis.unprocessed(item);

                if (analyzeImmediately) {
                    analysis = performAnalysis(analysis);
                }

                if (!repoMap.isEmpty()) {
                    repoMap.values().iterator().next().save(analysis);
                }
            }
        }
    }

    public void processExistingData(String topic) {
        System.out.println("Scanning database for un-analyzed items: " + topic);
        int count = 0;

        for (MediaRepository repo : repoMap.values()) {
            // Fetch wrapper objects
            List<MediaAnalysis> items = repo.findByTopic(topic);
            System.out.println("Found " + items.size() + " items in repo. Checking for missing analysis...");

            for (MediaAnalysis analysis : items) {
                boolean needsAnalysis = (analysis.getSentiment().getValue() == 0.0) ||
                        (analysis.getDamageCategory() == DamageCategory.UNKNOWN);

                if (needsAnalysis) {
                    System.out.println(" -> Analyzing: " + truncate(analysis.getMedia().getContent()) + "...");

                    // Create NEW wrapper with results (Immutability)
                    MediaAnalysis updated = performAnalysis(analysis);

                    repo.updateAnalysis(updated);
                    count++;

                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
        }
        System.out.println("Batch Analysis Complete. Updated " + count + " items.");
    }

    private MediaAnalysis performAnalysis(MediaAnalysis input) {
        Media media = input.getMedia();
        String textToAnalyze = media.getContent();

        // 1. Fetch full content if URL exists
        String url = media.getUrl();
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
        double scoreVal = 0.0;
        try {
            scoreVal = sentimentAnalyzer.analyzeScore(textToAnalyze);
        } catch (Exception e) {
            System.err.println("Sentiment Error: " + e.getMessage());
        }

        // 3. Run Damage Classification
        DamageCategory cat = DamageCategory.UNKNOWN;
        try {
            if (damageClassifier != null) {
                cat = damageClassifier.classify(textToAnalyze);
                System.out.println("   [RESULT] Score: " + scoreVal + " | Type: " + cat);
            }
        } catch (Exception e) {
            System.err.println("Classification Error: " + e.getMessage());
        }

        // Return new immutable instance with results
        return input.withSentiment(SentimentScore.of(scoreVal))
                .withDamageCategory(cat);
    }

    private String truncate(String input) {
        if (input == null) return "";
        return input.length() > 50 ? input.substring(0, 50) : input;
    }
}