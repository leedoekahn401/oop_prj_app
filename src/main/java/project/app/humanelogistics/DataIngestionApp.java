package project.app.humanelogistics;

import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.preprocessing.GoogleNewsCollector;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.AnalysisService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class DataIngestionApp {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   HUMANE LOGISTICS - DATA INGESTION TOOL");
        System.out.println("==========================================");

        // 1. Setup Dependencies
        String dbConn = Config.getDbConnectionString();
        MediaRepository repo = new MongoMediaRepository(dbConn, "storm_data", "news");
        SentimentGrade analyzer = new SentimentGrade();

        // FIX: Constructor now only takes the analyzer
        AnalysisService service = new AnalysisService(analyzer);

        // FIX: Add the repository explicitly with a label
        service.addRepository("News", repo);

        Scanner scanner = new Scanner(System.in);

        // 2. CONFIGURATION INPUTS
        System.out.println("--- Configuration ---");

        // Topic
        System.out.print("Enter Search Topic (default: Typhoon Yagi): ");
        String topicInput = scanner.nextLine().trim();
        String topic = topicInput.isEmpty() ? "Typhoon Yagi" : topicInput;

        // Default Dates
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");
        String defaultStart = "9/4/2024";
        String defaultEnd = today.format(fmt);

        // Start Date
        System.out.print("Enter Start Date [M/d/yyyy] (default: " + defaultStart + "): ");
        String startInput = scanner.nextLine().trim();
        String startDate = startInput.isEmpty() ? defaultStart : startInput;

        // End Date
        System.out.print("Enter End Date [M/d/yyyy] (default: " + defaultEnd + "): ");
        String endInput = scanner.nextLine().trim();
        String endDate = endInput.isEmpty() ? defaultEnd : endInput;

        System.out.println("\nUsing Topic: " + topic);
        System.out.println("Date Range:  " + startDate + " -> " + endDate);

        // 3. Interactive Menu
        System.out.println("\n--- Select Mode ---");
        System.out.println("   [1] Search Only (Collect data, No AI Grading)");
        System.out.println("   [2] Grade Only  (Process existing DB items)");
        System.out.println("   [3] Full Cycle  (Search + Grade immediately)");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        // 4. Execution Logic
        if (choice.equals("1")) {
            System.out.println("\n>>> STARTING SEARCH ONLY <<<");
            service.clearCollectors();
            service.registerCollectors(new GoogleNewsCollector());
            // false = do not grade
            service.processNewData(topic, startDate, endDate, false);
        }
        else if (choice.equals("2")) {
            System.out.println("\n>>> STARTING GRADE ONLY <<<");
            service.processMissingSentiments(topic);
        }
        else if (choice.equals("3")) {
            System.out.println("\n>>> STARTING FULL CYCLE <<<");
            service.clearCollectors();
            service.registerCollectors(new GoogleNewsCollector());
            // true = grade immediately
            service.processNewData(topic, startDate, endDate, true);
        }
        else {
            System.out.println("Invalid choice. Exiting.");
        }

        System.out.println("\n==========================================");
        System.out.println("   OPERATION COMPLETE");
        System.out.println("==========================================");

        scanner.close();
    }
}