package project.app.humanelogistics;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.preprocessing.ContentClassifier;
import project.app.humanelogistics.preprocessing.GeminiDamageClassifier;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.*;

public class ApplicationBootstrap {

    private static RepositoryFactory repositoryFactory;

    // Exposed Services for Dependency Injection
    private static DashboardService dashboardService;
    private static NavigationService navigationService;
    private static ChartService chartService;
    private static AnalysisService analysisService;

    public static void initialize() {
        System.out.println("Initializing Humane Logistics Application...");

        try {
            AppConfig config = AppConfig.getInstance();
            repositoryFactory = new RepositoryFactory(config);

            // Create core services
            SentimentAnalyzer sentimentAnalyzer = new SentimentGrade();
            ContentClassifier damageClassifier = new GeminiDamageClassifier();
            SummaryGenerator summaryGenerator = new GeminiSummaryGenerator();

            analysisService = new AnalysisService(
                    sentimentAnalyzer,
                    damageClassifier,
                    summaryGenerator
            );

            analysisService.addRepository("News", repositoryFactory.getNewsRepository());
            analysisService.addRepository("Social Posts", repositoryFactory.getSocialPostRepository());

            dashboardService = new DashboardService(
                    analysisService,
                    config.getDefaultTopic(),
                    config.getAnalysisYear()
            );

            navigationService = new NavigationService();
            chartService = new ChartService();

            registerShutdownHook();
            System.out.println("Application initialized successfully!");

        } catch (Exception e) {
            System.err.println("CRITICAL: Application initialization failed!");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize application", e);
        }
    }

    // --- Getters for Main and DataIngestionApp to use ---
    public static DashboardService getDashboardService() { return dashboardService; }
    public static NavigationService getNavigationService() { return navigationService; }
    public static ChartService getChartService() { return chartService; }
    public static AnalysisService getAnalysisService() { return analysisService; }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cleanup();
        }));
    }

    public static void cleanup() {
        if (repositoryFactory != null) {
            try {
                repositoryFactory.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}