package project.app.humanelogistics.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import project.app.humanelogistics.Config;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.preprocessing.GeminiDamageClassifier;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.AnalysisService;
import project.app.humanelogistics.service.SentimentDisplay;

import org.jfree.data.category.DefaultCategoryDataset;

public class DashBoardController {

    private static final String TOPIC_NAME = "Typhoon Yagi";

    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;

    private AnalysisService model;

    @FXML
    public void initialize() {
        String dbConn = Config.getDbConnectionString();
        MediaRepository newsRepo = new MongoMediaRepository(dbConn, "storm_data", "news");
        MediaRepository socialRepo = new MongoMediaRepository(dbConn, "storm_data", "posts");

        this.model = new AnalysisService(new SentimentGrade(), new GeminiDamageClassifier());
        this.model.addRepository("News", newsRepo);
        this.model.addRepository("Social Posts", socialRepo);

        refreshDashboardStats();
    }

    private void refreshDashboardStats() {
        if(lblTotalPosts == null) return;
        lblTotalPosts.setText("Loading...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int total = model.getTotalPostCount(TOPIC_NAME);
                double avg = model.getOverallAverageScore(TOPIC_NAME);

                // Use Value Object
                SentimentScore avgScore = SentimentScore.of(avg);

                // Calculate Top Damage
                DefaultCategoryDataset damageData = model.getDamageData(TOPIC_NAME);
                String topDmg = "None";
                double maxVal = 0;
                for(int i=0; i<damageData.getColumnCount(); i++) {
                    Number val = damageData.getValue(0, i);
                    if(val.doubleValue() > maxVal) {
                        maxVal = val.doubleValue();
                        topDmg = (String) damageData.getColumnKey(i);
                    }
                }

                String finalTopDmg = topDmg;
                int finalMaxVal = (int) maxVal;

                Platform.runLater(() -> {
                    lblTotalPosts.setText(String.valueOf(total));
                    lblSentimentScore.setText(String.format("%.2f", avgScore.getValue()));

                    // Fix: Tell, Don't Ask
                    // We don't check if(avg > 0.1) here anymore. We delegate to the Display object.
                    new SentimentDisplay(avgScore).applyTo(lblSentimentLabel);

                    lblTopDamage.setText(finalTopDmg);
                    lblTopDamageCount.setText(finalMaxVal + " reports");
                });
                return null;
            }
        };
        new Thread(task).start();
    }
}