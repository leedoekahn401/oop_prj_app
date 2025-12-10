package project.app.humanelogistics.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.service.*;
import project.app.humanelogistics.utils.AsyncTaskUtil;
import project.app.humanelogistics.view.DashboardView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DashboardController {

    // FXML Injected Components
    @FXML private VBox mainContent;
    @FXML private Button homeButton;
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
    private DashboardView viewManager;

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
            initializeView();
            setupNavigation();
            loadDashboardData();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    private void initializeView() {
        // Initialize ViewManager
        ObservableList<Node> defaultNodes = FXCollections.observableArrayList(mainContent.getChildren());
        this.viewManager = new DashboardView(mainContent, defaultNodes);

        // Bind the injected UI widgets to the ViewManager
        // The Controller "owns" the injection, but delegates "management" to ViewManager
        this.viewManager.bindStatsWidgets(
                lblTotalPosts, lblSentimentScore, lblSentimentLabel,
                lblTopDamage, lblTopDamageCount, lblSummary
        );
    }

    private void setupNavigation() {
        navigationService.registerButtons(homeButton, informationButton);
        homeButton.setOnAction(e -> showHome());
        informationButton.setOnAction(e -> showInformation());
    }

    private void loadDashboardData() {
        // Delegate view state logic
        viewManager.showStatsLoading();

        // 1. Load Statistics & Text Summary
        AsyncTaskUtil.execute(
                () -> dashboardService.getDashboardStats(),
                stats -> {
                    // Delegate view update logic
                    viewManager.updateDashboardStats(stats);

                    // 2. Trigger chart generation
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

    private void showHome() {
        navigationService.setActiveButton(homeButton);
        viewManager.showDefault();
    }

    private void showInformation() {
        navigationService.setActiveButton(informationButton);

        List<Developer> developers = List.of(
                new Developer("Hoang Hai Nam", "Developer", "/project/app/humanelogistics/picture1.png"),
                new Developer("Le Duc Anh", "Developer", "/project/app/humanelogistics/picture2.png")
        );

        viewManager.showDevelopers(developers);
    }

    private void handleError(Throwable error) {
        error.printStackTrace();
        viewManager.showError("Failed to load data: " + error.getMessage());
    }

    private void handleInitializationError(Exception e) {
        e.printStackTrace();
        // Fallback if viewManager failed
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