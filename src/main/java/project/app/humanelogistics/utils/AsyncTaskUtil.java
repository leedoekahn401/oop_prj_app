package project.app.humanelogistics.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncTaskUtil {

    public static <T> void execute(
            Supplier<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return backgroundTask.get();
            }
        };

        task.setOnSucceeded(event ->
                Platform.runLater(() -> onSuccess.accept(task.getValue())));

        task.setOnFailed(event ->
                Platform.runLater(() -> onError.accept(task.getException())));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public static void executeVoid(
            Runnable backgroundTask,
            Runnable onSuccess,
            Consumer<Throwable> onError) {

        execute(
                () -> { backgroundTask.run(); return null; },
                result -> onSuccess.run(),
                onError
        );
    }
}