package project.app.humanelogistics;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.preprocessing.analysis.ContentClassifier;
import project.app.humanelogistics.preprocessing.analysis.GeminiDamageClassifier;
import project.app.humanelogistics.preprocessing.IngestionPipeline; // NEW IMPORT
import project.app.humanelogistics.preprocessing.analysis.SentimentGrade;
import project.app.humanelogistics.preprocessing.analysis.SentimentAnalyzer;
import project.app.humanelogistics.service.*;

public class ApplicationBootstrap {

    private static RepositoryFactory repositoryFactory;
    private static DashboardService dashboardService;
    private static NavigationService navigationService;
    private static ChartService chartService;
    private static StatisticsService statisticsService;
    private static IngestionPipeline ingestionPipeline;

    public static void initialize() {
        System.out.println("Initializing Humane Logistics Application...");

        try {
            AppConfig config = AppConfig.getInstance();
            repositoryFactory = new RepositoryFactory(config);

            MediaRepository newsRepo = repositoryFactory.getNewsRepository();
            MediaRepository socialRepo = repositoryFactory.getSocialPostRepository();
            SentimentAnalyzer sentimentAnalyzer = new SentimentGrade();
            ContentClassifier damageClassifier = new GeminiDamageClassifier();
            ingestionPipeline = new IngestionPipeline(sentimentAnalyzer, damageClassifier);
            ingestionPipeline.addRepository("News", newsRepo);
            ingestionPipeline.addRepository("Social Posts", socialRepo);
            SummaryGenerator summaryGenerator = new GeminiSummaryGenerator();
            statisticsService = new StatisticsService(summaryGenerator);
            statisticsService.addRepository("News", newsRepo);
            statisticsService.addRepository("Social Posts", socialRepo);
            dashboardService = new DashboardService(
                    statisticsService,
                    config.getDefaultTopic(),
                    config.getAnalysisYear()
            );

            navigationService = new NavigationService();
            chartService = new ChartService();

            registerShutdownHook();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static DashboardService getDashboardService() { return dashboardService; }
    public static NavigationService getNavigationService() { return navigationService; }
    public static ChartService getChartService() { return chartService; }
    public static StatisticsService getStatisticsService() { return statisticsService; }


    public static IngestionPipeline getIngestionPipeline() { return ingestionPipeline; }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cleanup();
        }));
    }

    public static void cleanup() {
        if (repositoryFactory != null) {
            try {
                repositoryFactory.close();
                System.out.println("Resources cleaned up.");
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}