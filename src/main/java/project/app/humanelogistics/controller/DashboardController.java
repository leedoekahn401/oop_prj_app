package project.app.humanelogistics.controller;

import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.service.*;
import project.app.humanelogistics.utils.AsyncTaskUtil;
import project.app.humanelogistics.view.DashboardView;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    private final DashboardService dashboardService;
    private final NavigationService navigationService;
    private final ChartService chartService;

    private DashboardView view;

    public DashboardController(DashboardService dashboardService,
                               NavigationService navigationService,
                               ChartService chartService) {
        this.dashboardService = dashboardService;
        this.navigationService = navigationService;
        this.chartService = chartService;
    }

    public void setView(DashboardView view) {
        this.view = view;
        setupNavigation();
        loadDashboardData();
    }

    private void setupNavigation() {
        navigationService.registerButtons(view.getHomeButton(), view.getInformationButton());
        view.getHomeButton().setOnAction(e -> showHome());
        view.getInformationButton().setOnAction(e -> showInformation());
    }

    private void loadDashboardData() {
        view.showStatsLoading();

        AsyncTaskUtil.execute(
                () -> dashboardService.getDashboardStats(),
                stats -> {
                    view.updateDashboardStats(stats);
                    loadCharts();
                },
                this::handleError
        );
    }

    private void loadCharts() {

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
                chartData -> view.addDashboardChart("Sentiment Trends", chartData.getChartFile()),
                this::handleError
        );

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
                    view.addDashboardChart("Damage Distribution", chartData.getPieChart());
                    view.addDashboardChart("Damage Counts", chartData.getBarChart());
                },
                this::handleError
        );
    }

    private void showHome() {
        navigationService.setActiveButton(view.getHomeButton());
        view.showDefault();
    }

    private void showInformation() {
        navigationService.setActiveButton(view.getInformationButton());

        List<Developer> developers = List.of(
                new Developer("Hoang Hai Nam", "Developer", "/project/app/humanelogistics/picture1.png"),
                new Developer("Le Duc Anh", "Developer", "/project/app/humanelogistics/picture2.png")
        );

        view.showDevelopers(developers);
    }

    private void handleError(Throwable error) {
        error.printStackTrace();
        view.showError("Failed to load data: " + error.getMessage());
    }

    public static class SentimentChartData {
        private final java.io.File chartFile;

        public SentimentChartData(org.jfree.data.time.TimeSeriesCollection dataset,
                                  ChartService chartService) throws java.io.IOException {
            this.chartFile = chartService.generateAndSaveChart(
                    "Daily Sentiment Trend", "Date", "Score", dataset, "temp_sentiment.png"
            );
        }

        public java.io.File getChartFile() { return chartFile; }
    }

    public static class DamageChartData {
        private final java.io.File barChart;
        private final java.io.File pieChart;

        public DamageChartData(org.jfree.data.category.DefaultCategoryDataset dataset,
                               ChartService chartService) throws java.io.IOException {
            this.barChart = chartService.generateBarChart(
                    "Damage Counts", "Category", "Reports", dataset, "temp_damage_bar.png"
            );
            this.pieChart = chartService.generatePieChart(
                    "Damage Distribution", dataset, "temp_damage_pie.png"
            );
        }

        public java.io.File getBarChart() { return barChart; }
        public java.io.File getPieChart() { return pieChart; }
    }
}