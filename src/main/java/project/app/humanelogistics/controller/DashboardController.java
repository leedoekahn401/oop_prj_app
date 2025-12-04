package project.app.humanelogistics.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import project.app.humanelogistics.config.ServiceLocator;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.service.*;
import project.app.humanelogistics.utils.AsyncTaskUtil;
import project.app.humanelogistics.view.DashboardViewManager;

public class DashboardController {

    // FXML Injected Components
    @FXML private VBox mainContent;
    @FXML private Button homeButton;
    @FXML private Button sentimentButton;
    @FXML private Button inventoryButton;
    @FXML private Button informationButton;

    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;
    @FXML private Label lblSummary;

    // Services (Injected via ServiceLocator)
    private DashboardService dashboardService;
    private NavigationService navigationService;
    private ChartService chartService;
    private DashboardViewManager viewManager;

    @FXML
    public void initialize() {
        try {
            initializeServices();
            initializeView();
            setupNavigation();
            loadDashboardData();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    /**
     * FIXED: Use dependency injection instead of direct instantiation
     */
    private void initializeServices() {
        this.dashboardService = ServiceLocator.get(DashboardService.class);
        this.navigationService = ServiceLocator.get(NavigationService.class);
        this.chartService = ServiceLocator.get(ChartService.class);
    }

    /**
     * FIXED: Delegate view initialization to ViewManager
     */
    private void initializeView() {
        this.viewManager = new DashboardViewManager(mainContent);
        viewManager.initialize();
    }

    /**
     * FIXED: Simplified navigation setup
     */
    private void setupNavigation() {
        navigationService.registerButtons(
                homeButton, sentimentButton, inventoryButton, informationButton
        );

        homeButton.setOnAction(e -> showHome());
        sentimentButton.setOnAction(e -> showSentiment());
        inventoryButton.setOnAction(e -> showDamage());
        informationButton.setOnAction(e -> showInformation());
    }

    /**
     * FIXED: Controller only orchestrates, no business logic
     */
    private void loadDashboardData() {
        setLoadingState();

        AsyncTaskUtil.execute(
                () -> dashboardService.getDashboardStats(),
                this::updateDashboardUI,
                this::handleError
        );
    }

    /**
     * FIXED: Pure UI update, no calculations
     */
    private void updateDashboardUI(DashboardStats stats) {
        lblTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        lblSentimentScore.setText(String.format("%.2f", stats.getAvgSentiment()));

        // Use SentimentDisplay to apply styling
        SentimentScore sentimentScore = SentimentScore.of(stats.getAvgSentiment());
        new SentimentDisplay(sentimentScore).applyTo(lblSentimentLabel);

        lblTopDamage.setText(stats.getTopDamageCategory());
        lblTopDamageCount.setText(stats.getTopDamageCount() + " reports");
        lblSummary.setText(stats.getAiSummary());
    }

    private void setLoadingState() {
        lblTotalPosts.setText("Loading...");
        lblSentimentScore.setText("--");
        lblSentimentLabel.setText("...");
        lblSummary.setText("Analyzing data...");
    }

    // ========================================
    // NAVIGATION HANDLERS (Thin Orchestration)
    // ========================================

    private void showHome() {
        navigationService.setActiveButton(homeButton);
        viewManager.showDefault();
    }

    private void showSentiment() {
        navigationService.setActiveButton(sentimentButton);
        viewManager.showLoading("Generating Sentiment Analysis...");

        AsyncTaskUtil.execute(
                () -> new SentimentChartData(
                        dashboardService.getSentimentTimeSeries(),
                        chartService
                ),
                chartData -> viewManager.showSentimentChart(chartData),
                this::handleError
        );
    }

    private void showDamage() {
        navigationService.setActiveButton(inventoryButton);
        viewManager.showLoading("Analyzing Damage Reports...");

        AsyncTaskUtil.execute(
                () -> new DamageChartData(
                        dashboardService.getDamageDataset(),
                        chartService
                ),
                chartData -> viewManager.showDamageCharts(chartData),
                this::handleError
        );
    }

    private void showInformation() {
        navigationService.setActiveButton(informationButton);
        viewManager.showDevelopers();
    }

    // ========================================
    // ERROR HANDLING
    // ========================================

    private void handleError(Throwable error) {
        error.printStackTrace();
        viewManager.showError("Failed to load data: " + error.getMessage());
    }

    private void handleInitializationError(Exception e) {
        e.printStackTrace();
        if (lblTotalPosts != null) {
            lblTotalPosts.setText("Init Error");
        }
    }

    // ========================================
    // DATA TRANSFER OBJECTS FOR CHARTS
    // ========================================

    public static class SentimentChartData {
        private final java.io.File chartFile;

        public SentimentChartData(
                org.jfree.data.time.TimeSeriesCollection dataset,
                ChartService chartService) throws java.io.IOException {
            this.chartFile = chartService.generateAndSaveChart(
                    "Daily Sentiment Trend",
                    "Date",
                    "Score",
                    dataset,
                    "temp_sentiment.png"
            );
        }

        public java.io.File getChartFile() {
            return chartFile;
        }
    }

    public static class DamageChartData {
        private final java.io.File barChart;
        private final java.io.File pieChart;

        public DamageChartData(
                org.jfree.data.category.DefaultCategoryDataset dataset,
                ChartService chartService) throws java.io.IOException {
            this.barChart = chartService.generateBarChart(
                    "Damage Counts",
                    "Category",
                    "Reports",
                    dataset,
                    "temp_damage_bar.png"
            );
            this.pieChart = chartService.generatePieChart(
                    "Damage Distribution",
                    dataset,
                    "temp_damage_pie.png"
            );
        }

        public java.io.File getBarChart() {
            return barChart;
        }

        public java.io.File getPieChart() {
            return pieChart;
        }
    }
}
