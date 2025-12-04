package project.app.humanelogistics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void init() throws Exception {
        // FIXED: Initialize services before UI
        ApplicationBootstrap.initialize();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("hello-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Humane Logistics Data Application");
        stage.setScene(scene);

        // Handle window close
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
