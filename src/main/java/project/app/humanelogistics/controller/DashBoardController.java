package project.app.humanelogistics.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import project.app.humanelogistics.Config;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.preprocessing.GeminiDamageClassifier;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.AnalysisService;
import project.app.humanelogistics.service.ChartService;
import project.app.humanelogistics.service.GeminiSummaryGenerator;
import project.app.humanelogistics.service.SentimentDisplay;
import project.app.humanelogistics.view.DashboardView;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DashBoardController {

    private static final String TOPIC_NAME = "Typhoon Yagi";

    @FXML private VBox mainContent;

    @FXML private Button homeButton;
    @FXML private Button sentimentButton;
    @FXML private Button inventoryButton; // Damage Analysis
    @FXML private Button informationButton;

    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;
    @FXML private Label lblSummary;

    private AnalysisService model;
    private ChartService chartService;
    private DashboardView viewHelper;

    @FXML
    public void initialize() {
        System.out.println("Initializing Dashboard...");
        try {
            MediaRepository newsRepo = new MongoMediaRepository("storm_data", "news");
            MediaRepository socialRepo = new MongoMediaRepository("storm_data", "posts");

            this.model = new AnalysisService(
                    new SentimentGrade(),
                    new GeminiDamageClassifier(),
                    new GeminiSummaryGenerator()
            );

            this.model.addRepository("News", newsRepo);
            this.model.addRepository("Social Posts", socialRepo);

            this.chartService = new ChartService();

            // 2. Setup View Helper
            List<Node> homeNodes = new ArrayList<>(mainContent.getChildren());
            this.viewHelper = new DashboardView(mainContent, FXCollections.observableArrayList(homeNodes));

            // 3. Setup Navigation Handlers
            setupNavigation();

            // 4. Load Initial Data
            refreshDashboardStats();

            // 5. Ensure Scrolling (Robust Programmatic Fix)
            Platform.runLater(() -> {
                // Walk up the tree to find the root BorderPane
                Node currentNode = mainContent;
                while (currentNode != null) {
                    if (currentNode instanceof BorderPane) {
                        BorderPane rootLayout = (BorderPane) currentNode;
                        Node centerNode = rootLayout.getCenter();

                        // Only wrap if it's NOT already a ScrollPane
                        if (!(centerNode instanceof ScrollPane)) {
                            System.out.println("Applying ScrollPane fix...");
                            ScrollPane scrollPane = new ScrollPane(centerNode);
                            scrollPane.setFitToWidth(true);
                            scrollPane.setFitToHeight(false); // Allow vertical scrolling
                            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                            rootLayout.setCenter(scrollPane);
                        }
                        break;
                    }
                    currentNode = currentNode.getParent();
                }
            });

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during initialization: ");
            e.printStackTrace();
            if (lblTotalPosts != null) lblTotalPosts.setText("Init Error");
        }
    }

    private void setupNavigation() {
        homeButton.setOnAction(e -> {
            setActiveButton(homeButton);
            viewHelper.showDefault();
        });

        sentimentButton.setOnAction(e -> {
            setActiveButton(sentimentButton);
            loadSentimentView();
        });

        inventoryButton.setOnAction(e -> {
            setActiveButton(inventoryButton);
            loadDamageView();
        });

        informationButton.setOnAction(e -> {
            setActiveButton(informationButton);
            loadInfoView();
        });
    }

    private void setActiveButton(Button active) {
        homeButton.getStyleClass().remove("active");
        sentimentButton.getStyleClass().remove("active");
        inventoryButton.getStyleClass().remove("active");
        informationButton.getStyleClass().remove("active");

        active.getStyleClass().add("active");
    }

    // --- VIEW LOADING LOGIC ---

    private void loadSentimentView() {
        viewHelper.showLoading("Generating Sentiment Analysis...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                TimeSeriesCollection dataset = model.getSentimentData(TOPIC_NAME, 2024);

                File chartFile = chartService.generateAndSaveChart(
                        "Daily Sentiment Trend", "Date", "Score", dataset, "temp_sentiment.png"
                );

                Platform.runLater(() -> viewHelper.showChart("Sentiment Over Time", chartFile));
                return null;
            }
        };
        runTask(task);
    }

    private void loadDamageView() {
        viewHelper.showLoading("Analyzing Damage Reports...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DefaultCategoryDataset dataset = model.getDamageData(TOPIC_NAME);

                List<Pair<String, File>> charts = new ArrayList<>();

                // 1. Generate & Add Bar Chart
                File barChart = chartService.generateBarChart(
                        "Damage Counts", "Category", "Reports", dataset, "temp_damage_bar.png"
                );
                charts.add(new Pair<>("Distribution Analysis", barChart));

                // 2. Generate & Add Pie Chart
                File pieChart = chartService.generatePieChart(
                        "Damage Distribution", dataset, "temp_damage_pie.png"
                );
                charts.add(new Pair<>("Frequency Analysis", pieChart));

                Platform.runLater(() -> viewHelper.showChartGallery("Damage Assessment", charts));
                return null;
            }
        };
        runTask(task);
    }

    private void loadInfoView() {
        List<Developer> devs = new ArrayList<>();
        devs.add(new Developer("Nguyen Duc Anh", "Lead Developer", "/project/app/humanelogistics/picture1.jpg"));
        devs.add(new Developer("Vu Hieu Nghia", "AI Engineer", "/project/app/humanelogistics/picture2.jpg"));
        devs.add(new Developer("Truong Gia Binh", "Backend Engineer", "/project/app/humanelogistics/picture3.jpg"));

        viewHelper.showDevelopers(devs);
    }

    private void runTask(Task<?> task) {
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            Platform.runLater(() -> viewHelper.showError("Failed to load view: " + ex.getMessage()));
        });
        new Thread(task).start();
    }

    // --- DASHBOARD DATA LOADING ---

    private void refreshDashboardStats() {
        if(lblTotalPosts == null) return;
        lblTotalPosts.setText("Loading...");
        // Set loading state for summary
        if(lblSummary != null) lblSummary.setText("Consulting AI...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                // 1. Fetch Counts
                int total = model.getTotalPostCount(TOPIC_NAME);

                // 2. Calculate Average
                double avg = model.getOverallAverageScore(TOPIC_NAME);
                SentimentScore avgScore = SentimentScore.of(avg);

                // 3. Calculate Top Damage (Delegate to Service)
                String finalTopDmg = model.getTopDamageCategory(TOPIC_NAME);

                // Get count for the top damage for the UI (Optional check)
                DefaultCategoryDataset damageData = model.getDamageData(TOPIC_NAME);
                int finalMaxVal = 0;
                try {
                    Number val = damageData.getValue("Damage Reports", finalTopDmg);
                    if (val != null) finalMaxVal = val.intValue();
                } catch (Exception ignored) {}

                // 4. Generate AI Summary (Delegate to Service)
                String aiSummary = model.generateTopicInsight(TOPIC_NAME);

                int finalMaxValLocal = finalMaxVal;

                // 5. Update UI
                Platform.runLater(() -> {
                    lblTotalPosts.setText(String.valueOf(total));
                    lblSentimentScore.setText(String.format("%.2f", avgScore.getValue()));
                    new SentimentDisplay(avgScore).applyTo(lblSentimentLabel);
                    lblTopDamage.setText(finalTopDmg);
                    lblTopDamageCount.setText(finalMaxValLocal + " reports");

                    // Update Summary Label
                    if(lblSummary != null) {
                        lblSummary.setText(aiSummary);
                    }
                });
                return null;
            }
        };

        task.setOnFailed(workerStateEvent -> {
            Throwable e = task.getException();
            if (e != null) e.printStackTrace();
            Platform.runLater(() -> {
                lblTotalPosts.setText("Error");
                lblSentimentLabel.setText("Connection Failed");
                if(lblSummary != null) lblSummary.setText("Analysis Unavailable");
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}