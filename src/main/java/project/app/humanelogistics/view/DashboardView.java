package project.app.humanelogistics.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import project.app.humanelogistics.factory.UIFactory;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.service.DashboardStats;
import project.app.humanelogistics.service.SentimentDisplay;

import java.io.File;
import java.util.List;

public class DashboardView {

    @FXML private VBox mainContent;
    @FXML private Button homeButton;
    @FXML private Button informationButton;
    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;
    @FXML private Label lblSummary;

    private ObservableList<Node> defaultContent;

    @FXML
    public void initialize() {
        if (mainContent != null) {
            this.defaultContent = FXCollections.observableArrayList(mainContent.getChildren());
        }
    }

    public Button getHomeButton() { return homeButton; }
    public Button getInformationButton() { return informationButton; }

    public void updateDashboardStats(DashboardStats stats) {
        if (lblTotalPosts == null) return;

        lblTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        lblSentimentScore.setText(String.format("%.2f", stats.getAvgSentiment()));

        SentimentScore sentimentScore = SentimentScore.of(stats.getAvgSentiment());
        new SentimentDisplay(sentimentScore).applyTo(lblSentimentLabel);

        lblTopDamage.setText(stats.getTopDamageCategory());
        lblTopDamageCount.setText(stats.getTopDamageCount() + " reports");
        lblSummary.setText(stats.getAiSummary());
    }

    public void showStatsLoading() {
        if (lblTotalPosts == null) return;

        lblTotalPosts.setText("Loading...");
        lblSentimentScore.setText("--");
        lblSentimentLabel.setText("...");
        lblSummary.setText("Analyzing data...");
    }

    public void showDefault() {
        if (defaultContent != null && mainContent != null) {
            mainContent.getChildren().setAll(defaultContent);
        }
    }

    public void addDashboardChart(String title, File chartFile) {
        if (mainContent == null) return;

        VBox chartBox = UIFactory.createChartContainer(title, chartFile);
        defaultContent.add(chartBox);

        if (!mainContent.getChildren().contains(chartBox)) {
            mainContent.getChildren().add(chartBox);
        }
    }

    public void showLoading(String message) {
        if (mainContent == null) return;
        mainContent.getChildren().clear();
        mainContent.getChildren().add(UIFactory.createLoadingText(message));
    }

    public void showError(String message) {
        if (mainContent == null) return;
        mainContent.getChildren().setAll(UIFactory.createErrorBox(message));
    }

    public void showChartGallery(String mainTitle, List<Pair<String, File>> charts) {
        if (mainContent == null) return;

        mainContent.getChildren().clear();
        Text header = UIFactory.createSectionHeader(mainTitle);
        mainContent.getChildren().add(header);

        if (charts == null || charts.isEmpty()) {
            mainContent.getChildren().add(UIFactory.createErrorBox("No charts generated."));
            return;
        }

        for (Pair<String, File> chartData : charts) {
            String chartTitle = chartData.getKey();
            File chartFile = chartData.getValue();
            VBox chartBox = UIFactory.createChartContainer(chartTitle, chartFile);
            mainContent.getChildren().add(chartBox);
        }
    }

    public void showChart(String title, File chartFile) {
        showChartGallery(title, List.of(new Pair<>(title, chartFile)));
    }

    public void showDevelopers(List<Developer> developers) {
        if (mainContent == null) return;

        mainContent.getChildren().clear();
        mainContent.getChildren().add(UIFactory.createSectionHeader("About Developers"));

        for (Developer dev : developers) {
            mainContent.getChildren().add(
                    UIFactory.createMemberCard(dev.getName(), dev.getRole(), dev.getImagePath())
            );
        }
    }
}