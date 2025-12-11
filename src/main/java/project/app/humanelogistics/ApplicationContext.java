package project.app.humanelogistics;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.preprocessing.IngestionPipeline;
import project.app.humanelogistics.preprocessing.analysis.ContentClassifier;
import project.app.humanelogistics.preprocessing.analysis.DamageClassifier;
import project.app.humanelogistics.preprocessing.analysis.SentimentAnalyzer;
import project.app.humanelogistics.preprocessing.analysis.SentimentGrade;
import project.app.humanelogistics.service.*;

public class ApplicationContext implements AutoCloseable {

    private final AppConfig config;
    private final RepositoryFactory repositoryFactory;

    private final MediaRepository newsRepository;
    private final MediaRepository socialPostRepository;

    private final DashboardService dashboardService;
    private final NavigationService navigationService;
    private final ChartService chartService;
    private final StatisticsService statisticsService;
    private final IngestionPipeline ingestionPipeline;

    public ApplicationContext(
            AppConfig config,
            RepositoryFactory repositoryFactory,
            SentimentAnalyzer sentimentAnalyzer,
            ContentClassifier damageClassifier,
            SummaryGenerator summaryGenerator) {

        this.config = config;
        this.repositoryFactory = repositoryFactory;

        this.newsRepository = repositoryFactory.getNewsRepository();
        this.socialPostRepository = repositoryFactory.getSocialPostRepository();

        this.ingestionPipeline = new IngestionPipeline(sentimentAnalyzer, damageClassifier);
        this.ingestionPipeline.addRepository("News", newsRepository);
        this.ingestionPipeline.addRepository("Social Posts", socialPostRepository);

        this.statisticsService = new StatisticsService(summaryGenerator);
        this.statisticsService.addRepository("News", newsRepository);
        this.statisticsService.addRepository("Social Posts", socialPostRepository);

        this.dashboardService = new DashboardService(
                statisticsService,
                config.getDefaultTopic(),
                config.getAnalysisYear()
        );

        this.navigationService = new NavigationService();
        this.chartService = new ChartService();
    }

    public static ApplicationContext createProductionContext() {
        AppConfig config = AppConfig.getInstance();
        RepositoryFactory repositoryFactory = new RepositoryFactory(config);
        SentimentAnalyzer sentimentAnalyzer = new SentimentGrade();
        ContentClassifier damageClassifier = new DamageClassifier();
        SummaryGenerator summaryGenerator = new GeminiSummaryGenerator();

        return new ApplicationContext(
                config,
                repositoryFactory,
                sentimentAnalyzer,
                damageClassifier,
                summaryGenerator
        );
    }

    public static ApplicationContext createTestContext(
            MediaRepository mockNewsRepo,
            MediaRepository mockSocialRepo,
            SentimentAnalyzer mockSentiment,
            SummaryGenerator mockSummary) {

        AppConfig testConfig = AppConfig.getInstance();

        RepositoryFactory testFactory = new RepositoryFactory(testConfig) {
            @Override
            public MediaRepository getNewsRepository() { return mockNewsRepo; }

            @Override
            public MediaRepository getSocialPostRepository() { return mockSocialRepo; }
        };

        return new ApplicationContext(
                testConfig,
                testFactory,
                mockSentiment,
                new DamageClassifier(),

                mockSummary
        );
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public NavigationService getNavigationService() {
        return navigationService;
    }

    public ChartService getChartService() {
        return chartService;
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public IngestionPipeline getIngestionPipeline() {
        return ingestionPipeline;
    }

    public MediaRepository getNewsRepository() {
        return newsRepository;
    }

    public MediaRepository getSocialPostRepository() {
        return socialPostRepository;
    }

    public AppConfig getConfig() {
        return config;
    }

    @Override
    public void close() {
        if (repositoryFactory != null) {
            try {
                repositoryFactory.close();
                System.out.println("ApplicationContext: Resources cleaned up.");
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}

