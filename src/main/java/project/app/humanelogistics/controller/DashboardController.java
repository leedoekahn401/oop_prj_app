package project.app.humanelogistics.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import project.app.humanelogistics.config.ServiceLocator;
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
    @FXML private Button sentimentButton;
    @FXML private Button inventoryButton;
    @FXML private Button informationButton;

    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;
    @FXML private Label lblSummary;

    // Services
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

    private void initializeServices() {
        this.dashboardService = ServiceLocator.get(DashboardService.class);
        this.navigationService = ServiceLocator.get(NavigationService.class);
        this.chartService = ServiceLocator.get(ChartService.class);
    }

    private void initializeView() {
        // Capture the default content from FXML before we modify the container
        ObservableList<Node> defaultNodes = FXCollections.observableArrayList(mainContent.getChildren());

        // Initialize ViewManager with container AND default content
        this.viewManager = new DashboardViewManager(mainContent, defaultNodes);
    }

    private void setupNavigation() {
        navigationService.registerButtons(
                homeButton, sentimentButton, inventoryButton, informationButton
        );

        homeButton.setOnAction(e -> showHome());
        sentimentButton.setOnAction(e -> showSentiment());
        inventoryButton.setOnAction(e -> showDamage());
        informationButton.setOnAction(e -> showInformation());
    }

    private void loadDashboardData() {
        setLoadingState();
        AsyncTaskUtil.execute(
                () -> dashboardService.getDashboardStats(),
                this::updateDashboardUI,
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

    // ========================================
    // NAVIGATION HANDLERS
    // ========================================

    private void showHome() {
        navigationService.setActiveButton(homeButton);
        viewManager.showDefault();
    }

    private void showSentiment() {
        navigationService.setActiveButton(sentimentButton);
        viewManager.showLoading("Generating Sentiment Analysis...");

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
                // FIXED: Used generic showChart method instead of missing showSentimentChart
                chartData -> viewManager.showChart("Sentiment Trends", chartData.getChartFile()),
                this::handleError
        );
    }

    private void showDamage() {
        navigationService.setActiveButton(inventoryButton);
        viewManager.showLoading("Analyzing Damage Reports...");

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
                chartData -> viewManager.showChartGallery("Damage Analysis",
                        List.of(
                                new Pair<>("Distribution", chartData.getPieChart()),
                                new Pair<>("Counts", chartData.getBarChart())
                        )
                ),
                this::handleError
        );
    }

    private void showInformation() {
        navigationService.setActiveButton(informationButton);
        // Pass empty list or fetch developers from a service if available
        viewManager.showDevelopers(Collections.emptyList());
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