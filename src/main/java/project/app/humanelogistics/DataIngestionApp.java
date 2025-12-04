package project.app.humanelogistics;

import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.config.ServiceLocator;
import project.app.humanelogistics.factory.RepositoryFactory;
import project.app.humanelogistics.preprocessing.GoogleNewsCollector;
import project.app.humanelogistics.service.AnalysisService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class DataIngestionApp {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   HUMANE LOGISTICS - DATA INGESTION TOOL");
        System.out.println("==========================================");

        // FIXED: Use centralized initialization
        ApplicationBootstrap.initialize();

        // Get services from container
        AnalysisService service = ServiceLocator.get(AnalysisService.class);
        AppConfig config = AppConfig.getInstance();

        Scanner scanner = new Scanner(System.in);

        // Configuration inputs
        System.out.println("--- Configuration ---");

        System.out.print("Enter Search Topic (default: " + config.getDefaultTopic() + "): ");
        String topicInput = scanner.nextLine().trim();
        String topic = topicInput.isEmpty() ? config.getDefaultTopic() : topicInput;

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");
        String defaultStart = "9/4/2024";
        String defaultEnd = today.format(fmt);

        System.out.print("Enter Start Date [M/d/yyyy] (default: " + defaultStart + "): ");
        String startInput = scanner.nextLine().trim();
        String startDate = startInput.isEmpty() ? defaultStart : startInput;

        System.out.print("Enter End Date [M/d/yyyy] (default: " + defaultEnd + "): ");
        String endInput = scanner.nextLine().trim();
        String endDate = endInput.isEmpty() ? defaultEnd : endInput;

        System.out.println("\nUsing Topic: " + topic);
        System.out.println("Date Range:  " + startDate + " -> " + endDate);

        // Interactive Menu
        System.out.println("\n--- Select Mode ---");
        System.out.println("   [1] Search Only  (Collect data, No AI)");
        System.out.println("   [2] Search + Analyze (Full Cycle)");
        System.out.println("   [3] Analyze Only (Process existing DB items)");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        // Execution Logic
        if (choice.equals("1")) {
            System.out.println("\n>>> STARTING SEARCH ONLY <<<");
            service.clearCollectors();
            service.registerCollectors(new GoogleNewsCollector());
            service.processNewData(topic, startDate, endDate, false);
        }
        else if (choice.equals("2")) {
            System.out.println("\n>>> STARTING FULL ANALYSIS <<<");
            service.clearCollectors();
            service.registerCollectors(new GoogleNewsCollector());
            service.processNewData(topic, startDate, endDate, true);
        }
        else if (choice.equals("3")) {
            System.out.println("\n>>> STARTING ANALYSIS ONLY (Existing Data) <<<");
            service.processExistingData(topic);
        }
        else {
            System.out.println("Invalid choice. Exiting.");
        }

        System.out.println("\n==========================================");
        System.out.println("   OPERATION COMPLETE");
        System.out.println("==========================================");

        scanner.close();
        ApplicationBootstrap.cleanup();
    }
}