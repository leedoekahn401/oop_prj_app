package project.app.humanelogistics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.app.humanelogistics.controller.DashboardController;
import project.app.humanelogistics.view.DashboardView;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void init() throws Exception {
        ApplicationBootstrap.initialize();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("hello-view.fxml")
        );


        fxmlLoader.load();

        DashboardView view = fxmlLoader.getController();

        DashboardController controller = new DashboardController(
                ApplicationBootstrap.getDashboardService(),
                ApplicationBootstrap.getNavigationService(),
                ApplicationBootstrap.getChartService()
        );

        controller.setView(view);

        Scene scene = new Scene(fxmlLoader.getRoot(), 1280, 800);
        stage.setTitle("Humane Logistics Data Application");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> ApplicationBootstrap.cleanup());

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ApplicationBootstrap.cleanup();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}