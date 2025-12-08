package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.MediaAnalysis;
import project.app.humanelogistics.preprocessing.collector.DataCollector;
import project.app.humanelogistics.preprocessing.collector.GoogleNewsCollector;

import java.util.List;

public class NewsIngestionTask {

    public static void main(String[] args) {
        System.out.println("--- Starting News Ingestion Task ---");

        try (RepositoryFactory factory = new RepositoryFactory(AppConfig.getInstance())) {

            MediaRepository newsRepo = factory.getNewsRepository();
            DataCollector collector = new GoogleNewsCollector();

            String query = "Typhoon Yagi Bão Yagi Vietnam news";
            String startDate = "9/5/2024";
            String endDate = "9/10/2024";

            System.out.println("Collector: Google News");
            System.out.println("Target DB Collection: news");
            System.out.println("Query: " + query);

            List<Media> articles = collector.collect(query, startDate, endDate, 3);
            System.out.println("Fetched " + articles.size() + " articles.");

            System.out.println("Saving to MongoDB...");
            int savedCount = 0;
            for (Media article : articles) {
                try {
                    // Wrap in analysis before saving
                    newsRepo.save(MediaAnalysis.unprocessed(article));
                    savedCount++;
                } catch (Exception e) {
                    System.err.println("Error saving article: " + e.getMessage());
                }
            }

            System.out.println("--- Ingestion Complete. Saved " + savedCount + " articles to 'news' collection. ---");

        } catch (Exception e) {
            System.err.println("Ingestion Task Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}