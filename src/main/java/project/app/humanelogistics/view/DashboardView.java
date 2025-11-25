package project.app.humanelogistics.view;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.util.UIFactory;

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

    public void showLoading(String message) {
        container.getChildren().clear();
        container.getChildren().add(UIFactory.createLoadingText(message));
    }

    public void showError(String message) {
        container.getChildren().setAll(UIFactory.createErrorBox(message));
    }

    public void showChart(String title, File chartFile) {
        VBox chartBox = UIFactory.createChartContainer(title, chartFile);
        container.getChildren().setAll(chartBox);
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