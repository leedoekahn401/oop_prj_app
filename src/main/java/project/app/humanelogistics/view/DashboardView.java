package project.app.humanelogistics.view;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.model.SentimentScore;
import project.app.humanelogistics.service.DashboardStats;
import project.app.humanelogistics.service.SentimentDisplay;
import project.app.humanelogistics.factory.UIFactory;

import java.io.File;
import java.util.List;

public class DashboardView {

    private final VBox container;
    private final ObservableList<Node> defaultContent;

    // --- UI Widgets managed by this View Class ---
    private Label lblTotalPosts;
    private Label lblSentimentScore;
    private Label lblSentimentLabel;
    private Label lblTopDamage;
    private Label lblTopDamageCount;
    private Label lblSummary;

    public DashboardView(VBox container, ObservableList<Node> defaultContent) {
        this.container = container;
        this.defaultContent = defaultContent;
    }

    /**
     * Binds the specific FXML labels to this view manager.
     * This allows the Controller to delegate all UI updates to this class.
     */
    public void bindStatsWidgets(Label lblTotalPosts, Label lblSentimentScore,
                                 Label lblSentimentLabel, Label lblTopDamage,
                                 Label lblTopDamageCount, Label lblSummary) {
        this.lblTotalPosts = lblTotalPosts;
        this.lblSentimentScore = lblSentimentScore;
        this.lblSentimentLabel = lblSentimentLabel;
        this.lblTopDamage = lblTopDamage;
        this.lblTopDamageCount = lblTopDamageCount;
        this.lblSummary = lblSummary;
    }

    // --- View Logic: Update Stats ---
    public void updateDashboardStats(DashboardStats stats) {
        if (lblTotalPosts == null) return;

        lblTotalPosts.setText(String.valueOf(stats.getTotalPosts()));
        lblSentimentScore.setText(String.format("%.2f", stats.getAvgSentiment()));

        // Encapsulated View Logic: Applying styles based on data
        SentimentScore sentimentScore = SentimentScore.of(stats.getAvgSentiment());
        new SentimentDisplay(sentimentScore).applyTo(lblSentimentLabel);

        lblTopDamage.setText(stats.getTopDamageCategory());
        lblTopDamageCount.setText(stats.getTopDamageCount() + " reports");
        lblSummary.setText(stats.getAiSummary());
    }

    // --- View Logic: Loading State ---
    public void showStatsLoading() {
        if (lblTotalPosts == null) return;

        lblTotalPosts.setText("Loading...");
        lblSentimentScore.setText("--");
        lblSentimentLabel.setText("...");
        lblSummary.setText("Analyzing data...");
    }

    // --- Existing Navigation Logic ---

    public void showDefault() {
        if (defaultContent != null) {
            container.getChildren().setAll(defaultContent);
        }
    }

    public void addDashboardChart(String title, File chartFile) {
        VBox chartBox = UIFactory.createChartContainer(title, chartFile);
        defaultContent.add(chartBox);
        if (!container.getChildren().contains(chartBox)) {
            container.getChildren().add(chartBox);
        }
    }

    public void showLoading(String message) {
        container.getChildren().clear();
        container.getChildren().add(UIFactory.createLoadingText(message));
    }

    public void showError(String message) {
        container.getChildren().setAll(UIFactory.createErrorBox(message));
    }

    public void showChartGallery(String mainTitle, List<Pair<String, File>> charts) {
        container.getChildren().clear();
        Text header = UIFactory.createSectionHeader(mainTitle);
        container.getChildren().add(header);

        if (charts == null || charts.isEmpty()) {
            container.getChildren().add(UIFactory.createErrorBox("No charts generated."));
            return;
        }

        for (Pair<String, File> chartData : charts) {
            String chartTitle = chartData.getKey();
            File chartFile = chartData.getValue();
            VBox chartBox = UIFactory.createChartContainer(chartTitle, chartFile);
            container.getChildren().add(chartBox);
        }
    }

    public void showChart(String title, File chartFile) {
        showChartGallery(title, List.of(new Pair<>(title, chartFile)));
    }

    public void showDevelopers(List<Developer> developers) {
        container.getChildren().clear();
        container.getChildren().add(UIFactory.createSectionHeader("About Developers"));

        for (Developer dev : developers) {
            container.getChildren().add(
                    UIFactory.createMemberCard(dev.getName(), dev.getRole(), dev.getImagePath())
            );
        }
    }
}