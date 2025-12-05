package project.app.humanelogistics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.app.humanelogistics.controller.DashboardController;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void init() throws Exception {
        // 1. Initialize all services
        ApplicationBootstrap.initialize();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("hello-view.fxml")
        );

        // 2. DEFINE THE CONTROLLER FACTORY
        // This tells JavaFX: "When you need a Controller, ask me first."
        fxmlLoader.setControllerFactory(controllerClass -> {

            if (controllerClass == DashboardController.class) {
                // INJECT DEPENDENCIES HERE
                return new DashboardController(
                        ApplicationBootstrap.getDashboardService(),
                        ApplicationBootstrap.getNavigationService(),
                        ApplicationBootstrap.getChartService()
                );
            }

            // Default behavior for other controllers (like InformationButtonController)
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not create controller: " + controllerClass.getName(), e);
            }
        });

        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        stage.setTitle("Humane Logistics Data Application");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            ApplicationBootstrap.cleanup();
        });

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