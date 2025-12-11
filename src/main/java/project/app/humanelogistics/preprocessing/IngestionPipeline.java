package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.*;
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
        System.out.println("Repositories registered: " + repoMap.keySet());

        if (repoMap.isEmpty()) {
            System.err.println("CRITICAL ERROR: No repositories registered. Data cannot be saved.");
            return;
        }

        for (DataCollector collector : collectors) {
            System.out.println("Invoking Collector: " + collector.getClass().getSimpleName());
            List<Media> freshData = collector.collect(topic, startDate, endDate, 1);
            System.out.println("  > Collector (" + collector.getClass().getSimpleName() + ") found " + freshData.size() + " raw items.");

            if (freshData.isEmpty()) {
                System.out.println("    (No data found by collector for this date range)");
                continue;
            }

            int successCount = 0;
            int duplicateCount = 0;

            for(Media item : freshData) {
                MediaAnalysis analysis = MediaAnalysis.unprocessed(item);

                if (analyzeImmediately) {
                    analysis = performAnalysis(analysis);
                }

                MediaRepository targetRepo = null;
                String targetRepoName = "";

                if (item instanceof News) {
                    targetRepo = repoMap.get("News");
                    targetRepoName = "News";
                } else if (item instanceof SocialPost) {
                    targetRepo = repoMap.get("Social Posts");
                    targetRepoName = "Social Posts";
                }

                if (targetRepo == null && !repoMap.isEmpty()) {
                    targetRepo = repoMap.values().iterator().next();
                    targetRepoName = "Default";
                }

                if (targetRepo != null) {
                    try {
                        // FIX: Check boolean return value
                        boolean wasSaved = targetRepo.save(analysis);

                        if (wasSaved) {
                            System.out.println("   -> [SAVED] to " + targetRepoName + ": " + truncate(item.getContent()));
                            successCount++;
                        } else {
                            System.out.println("   -> [DUPLICATE - SKIPPED]: " + truncate(item.getContent()));
                            duplicateCount++;
                        }

                    } catch (Exception e) {
                        System.err.println("  ! Error saving to DB: " + e.getMessage());
                    }
                }
            }
            System.out.println("  > Batch Done. Saved: " + successCount + " | Duplicates: " + duplicateCount);
        }
    }

    public void processExistingData(String topic) {
        System.out.println("Scanning database for un-analyzed items: " + topic);
        int count = 0;

        for (MediaRepository repo : repoMap.values()) {
            List<MediaAnalysis> items = repo.findByTopic(topic);
            System.out.println("Found " + items.size() + " items in repo. Checking for missing analysis...");

            for (MediaAnalysis analysis : items) {
                boolean needsAnalysis = (analysis.getSentiment().getValue() == 0.0) ||
                        (analysis.getDamageCategory() == DamageCategory.UNKNOWN);

                if (needsAnalysis) {
                    System.out.println(" -> Analyzing: " + truncate(analysis.getMedia().getContent()) + "...");
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

        double scoreVal = 0.0;
        try {
            scoreVal = sentimentAnalyzer.analyzeScore(textToAnalyze);
        } catch (Exception e) {
            System.err.println("Sentiment Error: " + e.getMessage());
        }

        DamageCategory cat = DamageCategory.UNKNOWN;
        try {
            if (damageClassifier != null) {
                cat = damageClassifier.classify(textToAnalyze);
                System.out.println("   [RESULT] Score: " + scoreVal + " | Type: " + cat);
            }
        } catch (Exception e) {
            System.err.println("Classification Error: " + e.getMessage());
        }

        return input.withSentiment(SentimentScore.of(scoreVal))
                .withDamageCategory(cat);
    }

    private String truncate(String input) {
        if (input == null) return "";
        return input.length() > 50 ? input.substring(0, 50) + "..." : input;
    }
}