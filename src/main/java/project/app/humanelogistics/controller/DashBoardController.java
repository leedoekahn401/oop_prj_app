package project.app.humanelogistics.controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MongoPostRepository;
import project.app.humanelogistics.db.PostRepository;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.service.AnalysisService;
import project.app.humanelogistics.service.ChartService;
import project.app.humanelogistics.service.KeywordSentimentAnalyzer;
import project.app.humanelogistics.view.DashboardView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DashBoardController {

    private static final String TOPIC_NAME = "Typhoon Yagi";
    private static final String CHART_FILE_PATH = "sentiment_chart.png";

    // You should move this to a secure config file in a real app
    private static final String DB_CONN = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    @FXML private VBox mainContent;
    @FXML private ImageView imgLogo;
    @FXML private Button informationButton;
    @FXML private Button sentimentButton;
    @FXML private Button homeButton;

    private AnalysisService model;
    private ChartService chartService;
    private DashboardView view;

    @FXML
    public void initialize() {
        // Dependency Injection - Easy to swap MongoPostRepository for SQLPostRepository
        PostRepository repo = new MongoPostRepository(DB_CONN, "storm_data", "posts");

        // Use KeywordAnalyzer (loads from JSON)
        this.model = new AnalysisService(repo, new KeywordSentimentAnalyzer());
        this.chartService = new ChartService();

        loadLogo();

        var backup = (mainContent != null)
                ? FXCollections.observableArrayList(mainContent.getChildren())
                : FXCollections.<Node>emptyObservableList();

        this.view = new DashboardView(mainContent, backup);
        setupNavigation();
    }

    private void setupNavigation() {
        if (informationButton != null) informationButton.setOnAction(e -> handleShowInfo());
        if (sentimentButton != null) sentimentButton.setOnAction(e -> handleShowSentimentAnalysis());
        if (homeButton != null) homeButton.setOnAction(e -> handleShowDashboard());
    }

    private void handleShowDashboard() {
        updateActiveButton(homeButton);
        view.showDefault();
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
        view.showLoading("Analyzing sentiment counts for " + TOPIC_NAME + "...");

        Task<File> task = new Task<>() {
            @Override
            protected File call() throws Exception {
                // Fetch data and generate chart
                TimeSeriesCollection dataset = model.getSentimentData(TOPIC_NAME, 2024);
                return chartService.generateAndSaveChart(
                        TOPIC_NAME + ": Positive vs Negative", "Date", "Number of Posts", dataset, CHART_FILE_PATH
                );
            }
        };

        task.setOnSucceeded(e -> view.showChart("Sentiment Analysis: Post Volume", task.getValue()));
        task.setOnFailed(e -> view.showError("Failed to generate chart: " + e.getSource().getException().getMessage()));

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