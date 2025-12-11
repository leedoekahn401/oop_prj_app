package project.app.humanelogistics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import project.app.humanelogistics.controller.DashboardController;
import project.app.humanelogistics.view.DashboardView;

import java.io.IOException;

public class Main extends Application {

    private ApplicationContext context;

    @Override
    public void init() throws Exception {
        // Initialize context (replaces ApplicationBootstrap.initialize())
        this.context = ApplicationContext.createProductionContext();
        System.out.println("Initializing Humane Logistics Application...");
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("hello-view.fxml")
        );

        // Load FXML (creates DashboardView instance)
        fxmlLoader.load();

        // Get the View instance created by FXML
        DashboardView view = fxmlLoader.getController();

        // Create Controller with injected dependencies from context
        DashboardController controller = new DashboardController(
                context.getDashboardService(),
                context.getNavigationService(),
                context.getChartService()
        );

        // Inject View into Controller
        controller.setView(view);

        Scene scene = new Scene(fxmlLoader.getRoot(), 1280, 800);
        stage.setTitle("Humane Logistics Data Application");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> cleanup());

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        cleanup();
        super.stop();
    }

    private void cleanup() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
