package project.app.humanelogistics.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.service.*;
import project.app.humanelogistics.utils.AsyncTaskUtil;
import project.app.humanelogistics.view.DashboardViewManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DashboardController {

    // FXML Injected Components
    @FXML private VBox mainContent;
    @FXML private Button homeButton;
    // Removed specific sentiment/inventory buttons
    @FXML private Button informationButton;

    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;
    @FXML private Label lblSummary;

    // Services (Injected via Constructor)
    private final DashboardService dashboardService;
    private final NavigationService navigationService;
    private final ChartService chartService;

    // View Helper
    private DashboardViewManager viewManager;

    // --- CONSTRUCTOR INJECTION ---
    public DashboardController(DashboardService dashboardService,
                               NavigationService navigationService,
                               ChartService chartService) {
        this.dashboardService = dashboardService;
        this.navigationService = navigationService;
        this.chartService = chartService;
    }

    @FXML
    public void initialize() {
        try {
            // Note: Services are already initialized via constructor!
            initializeView();
            setupNavigation();
            loadDashboardData();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    private void initializeView() {
        ObservableList<Node> defaultNodes = FXCollections.observableArrayList(mainContent.getChildren());
        this.viewManager = new DashboardViewManager(mainContent, defaultNodes);
    }

    private void setupNavigation() {
        navigationService.registerButtons(
                homeButton, informationButton
        );

        homeButton.setOnAction(e -> showHome());
        informationButton.setOnAction(e -> showInformation());
    }

    private void loadDashboardData() {
        setLoadingState();

        // 1. Load Statistics & Text Summary first
        AsyncTaskUtil.execute(
                () -> dashboardService.getDashboardStats(),
                stats -> {
                    updateDashboardUI(stats);
                    // 2. After stats are loaded, trigger chart generation
                    loadCharts();
                },
                this::handleError
        );
    }

    private void loadCharts() {
        // Load Sentiment Chart
        AsyncTaskUtil.execute(
                () -> {
                    try {
                        return new SentimentChartData(
                                dashboardService.getSentimentTimeSeries(),
                                chartService
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                chartData -> viewManager.addDashboardChart("Sentiment Trends", chartData.getChartFile()),
                this::handleError
        );

        // Load Damage Charts
        AsyncTaskUtil.execute(
                () -> {
                    try {
                        return new DamageChartData(
                                dashboardService.getDamageDataset(),
                                chartService
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                chartData -> {
                    viewManager.addDashboardChart("Damage Distribution", chartData.getPieChart());
                    viewManager.addDashboardChart("Damage Counts", chartData.getBarChart());
                },
                this::handleError
        );
    }

    private void updateDashboardUI(DashboardStats stats) {
        lblTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        lblSentimentScore.setText(String.format("%.2f", stats.getAvgSentiment()));

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

    private void showHome() {
        navigationService.setActiveButton(homeButton);
        viewManager.showDefault();
    }

    private void showInformation() {
        navigationService.setActiveButton(informationButton);
        viewManager.showDevelopers(Collections.emptyList());
    }

    private void handleError(Throwable error) {
        error.printStackTrace();
        viewManager.showError("Failed to load data: " + error.getMessage());
    }

    private void handleInitializationError(Exception e) {
        e.printStackTrace();
        if (lblTotalPosts != null) lblTotalPosts.setText("Init Error");
    }

    // Chart DTOs
    public static class SentimentChartData {
        private final java.io.File chartFile;
        public SentimentChartData(org.jfree.data.time.TimeSeriesCollection dataset, ChartService chartService) throws java.io.IOException {
            this.chartFile = chartService.generateAndSaveChart("Daily Sentiment Trend", "Date", "Score", dataset, "temp_sentiment.png");
        }
        public java.io.File getChartFile() { return chartFile; }
    }

    public static class DamageChartData {
        private final java.io.File barChart;
        private final java.io.File pieChart;
        public DamageChartData(org.jfree.data.category.DefaultCategoryDataset dataset, ChartService chartService) throws java.io.IOException {
            this.barChart = chartService.generateBarChart("Damage Counts", "Category", "Reports", dataset, "temp_damage_bar.png");
            this.pieChart = chartService.generatePieChart("Damage Distribution", dataset, "temp_damage_pie.png");
        }
        public java.io.File getBarChart() { return barChart; }
        public java.io.File getPieChart() { return pieChart; }
    }
}