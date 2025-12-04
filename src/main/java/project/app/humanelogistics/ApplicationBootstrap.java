package project.app.humanelogistics;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.config.ServiceLocator;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.preprocessing.ContentClassifier; // Added Import
import project.app.humanelogistics.preprocessing.GeminiDamageClassifier;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.*;

/**
 * FIXED: Centralized initialization with proper dependency injection
 * Call this BEFORE launching JavaFX application
 */
public class ApplicationBootstrap {

    private static RepositoryFactory repositoryFactory;

    public static void initialize() {
        System.out.println("Initializing Humane Logistics Application...");

        try {
            AppConfig config = AppConfig.getInstance();

            // Create factories
            repositoryFactory = new RepositoryFactory(config);

            // Create core services
            SentimentAnalyzer sentimentAnalyzer = new SentimentGrade();
            ContentClassifier damageClassifier = new GeminiDamageClassifier();
            SummaryGenerator summaryGenerator = new GeminiSummaryGenerator();

            // Create analysis service
            AnalysisService analysisService = new AnalysisService(
                    sentimentAnalyzer,
                    damageClassifier,
                    summaryGenerator
            );

            analysisService.addRepository("News", repositoryFactory.getNewsRepository());
            analysisService.addRepository("Social Posts", repositoryFactory.getSocialPostRepository());

            // Create dashboard service
            DashboardService dashboardService = new DashboardService(
                    analysisService,
                    config.getDefaultTopic(),
                    config.getAnalysisYear()
            );

            // Create other services
            NavigationService navigationService = new NavigationService();
            ChartService chartService = new ChartService();

            // Register all services
            ServiceLocator.register(AnalysisService.class, analysisService);
            ServiceLocator.register(DashboardService.class, dashboardService);
            ServiceLocator.register(NavigationService.class, navigationService);
            ServiceLocator.register(ChartService.class, chartService);
            ServiceLocator.register(RepositoryFactory.class, repositoryFactory);

            // Register shutdown hook
            registerShutdownHook();

            System.out.println("Application initialized successfully!");

        } catch (Exception e) {
            System.err.println("CRITICAL: Application initialization failed!");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize application", e);
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down application...");
            cleanup();
        }));
    }

    public static void cleanup() {
        if (repositoryFactory != null) {
            try {
                repositoryFactory.close();
                System.out.println("Resources cleaned up successfully.");
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}