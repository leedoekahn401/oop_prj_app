package project.app.humanelogistics.view;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.factory.UIFactory;

import java.io.File;
import java.util.List;

public class DashboardView {

    private final VBox container;
    private final ObservableList<Node> defaultContent;

    public DashboardView(VBox container, ObservableList<Node> defaultContent) {
        this.container = container;
        this.defaultContent = defaultContent;
    }

    public void showDefault() {
        if (defaultContent != null) {
            container.getChildren().setAll(defaultContent);
        }
    }

    // --- NEW: Add chart to the Dashboard flow ---
    public void addDashboardChart(String title, File chartFile) {
        VBox chartBox = UIFactory.createChartContainer(title, chartFile);

        // 1. Add to the 'defaultContent' memory so it persists if user navigates away and back
        defaultContent.add(chartBox);

        // 2. If we are currently on the dashboard, add it to the view immediately
        // (Simple check: if container has the same items as defaultContent minus the new one)
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

    // Convenience wrapper for single charts
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