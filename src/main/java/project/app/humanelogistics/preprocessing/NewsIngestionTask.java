package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.model.Media;

import java.util.List;

public class NewsIngestionTask {

    public static void main(String[] args) {
        System.out.println("--- Starting News Ingestion Task ---");

        // FIXED: Use RepositoryFactory to manage the MongoClient resource automatically.
        // The try-with-resources block ensures the connection is closed when done.
        try (RepositoryFactory factory = new RepositoryFactory(AppConfig.getInstance())) {

            MediaRepository newsRepo = factory.getNewsRepository();

            // 2. COLLECT
            // We use the GoogleNewsCollector which returns a list of Medias (specifically News objects)
            DataCollector collector = new GoogleNewsCollector();

            String query = "Typhoon Yagi Bão Yagi Vietnam news";
            String startDate = "9/5/2024";
            String endDate = "9/10/2024";

            System.out.println("Collector: Google News");
            System.out.println("Target DB Collection: news");
            System.out.println("Query: " + query);

            // Collect data
            List<Media> articles = collector.collect(query, startDate, endDate, 3);
            System.out.println("Fetched " + articles.size() + " articles.");

            // 3. SAVE
            System.out.println("Saving to MongoDB...");
            int savedCount = 0;
            for (Media article : articles) {
                try {
                    newsRepo.save(article);
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