package project.app.humanelogistics.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.AnalysisService;
import project.app.humanelogistics.service.ChartService;
import project.app.humanelogistics.view.DashboardView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DashBoardController {

    private static final String TOPIC_NAME = "Typhoon Yagi";
    private static final String CHART_FILE_PATH = "sentiment_score_chart.png";
    // NOTE: In a real app, do not hardcode credentials.
    private static final String DB_CONN = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    @FXML private VBox mainContent;
    @FXML private ImageView imgLogo;
    @FXML private Button informationButton;
    @FXML private Button sentimentButton;
    @FXML private Button homeButton;

    // INJECTED LABELS
    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;

    private AnalysisService model;
    private ChartService chartService;
    private DashboardView view;

    @FXML
    public void initialize() {
        MediaRepository repo = new MongoMediaRepository(DB_CONN, "storm_data", "posts");
        this.model = new AnalysisService(repo, new SentimentGrade());
        this.chartService = new ChartService();

        loadLogo();

        var backup = (mainContent != null)
                ? FXCollections.observableArrayList(mainContent.getChildren())
                : FXCollections.<Node>emptyObservableList();

        this.view = new DashboardView(mainContent, backup);

        setupNavigation();

        // IMMEDIATE ACTION: Fetch and display stats
        refreshDashboardStats();
    }

    private void refreshDashboardStats() {
        // Run database fetch in a background thread to keep UI snappy
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // 1. Calculations
                int total = model.getTotalPostCount(TOPIC_NAME);
                double avg = model.getOverallAverageScore(TOPIC_NAME);

                // 2. Update UI (Must be on JavaFX Application Thread)
                Platform.runLater(() -> {
                    if (lblTotalPosts != null) {
                        lblTotalPosts.setText(String.valueOf(total));
                    }

                    if (lblSentimentScore != null) {
                        lblSentimentScore.setText(String.format("%.2f", avg));
                    }

                    if (lblSentimentLabel != null) {
                        if (avg > 0.1) {
                            lblSentimentLabel.setText("Positive");
                            lblSentimentLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); // Green
                        } else if (avg < -0.1) {
                            lblSentimentLabel.setText("Negative");
                            lblSentimentLabel.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;"); // Red
                        } else {
                            lblSentimentLabel.setText("Neutral");
                            lblSentimentLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold;"); // Grey
                        }
                    }
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void setupNavigation() {
        if (informationButton != null) informationButton.setOnAction(e -> handleShowInfo());
        if (sentimentButton != null) sentimentButton.setOnAction(e -> handleShowSentimentAnalysis());
        if (homeButton != null) homeButton.setOnAction(e -> handleShowDashboard());
    }

    private void handleShowDashboard() {
        updateActiveButton(homeButton);
        view.showDefault();
        // Refresh stats when user returns to home screen
        refreshDashboardStats();
    }

    private void handleShowInfo() {
        updateActiveButton(informationButton);
        List<Developer> devs = Arrays.asList(
                new Developer("Team Lead", "Backend & Analysis", "/project/app/humanelogistics/picture1.jpg"),
                new Developer("UI Designer", "Frontend & UX", "/project/app/humanelogistics/picture2.jpg")
        );
        view.showDevelopers(devs);
    }

    private void handleShowSentimentAnalysis() {
        updateActiveButton(sentimentButton);
        view.showLoading("Generating Sentiment Chart (Using Cached Data)...");

        Task<File> task = new Task<>() {
            @Override
            protected File call() throws Exception {
                // Pure DB retrieval, no analysis loop
                TimeSeriesCollection dataset = model.getSentimentData(TOPIC_NAME, 2024);
                return chartService.generateAndSaveChart(
                        TOPIC_NAME + ": Average Sentiment Trend",
                        "Date",
                        "Score (-1.0 to 1.0)",
                        dataset,
                        CHART_FILE_PATH
                );
            }
        };

        task.setOnSucceeded(e -> view.showChart("Sentiment Analysis: Average Score", task.getValue()));
        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            view.showError("Failed to generate chart: " + e.getSource().getException().getMessage());
        });
        new Thread(task).start();
    }

    private void loadLogo() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/project/app/humanelogistics/logo.png"));
            if (this.imgLogo != null) this.imgLogo.setImage(img);
        } catch (Exception e) { /* Ignore */ }
    }

    private void updateActiveButton(Button clicked) {
        if (homeButton != null) homeButton.getStyleClass().remove("active");
        if (sentimentButton != null) sentimentButton.getStyleClass().remove("active");
        if (informationButton != null) informationButton.getStyleClass().remove("active");
        if (clicked != null) clicked.getStyleClass().add("active");
    }
}