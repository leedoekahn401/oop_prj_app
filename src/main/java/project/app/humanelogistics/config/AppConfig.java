package project.app.humanelogistics.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;

public class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();
    private final Properties properties;

    private AppConfig() {
        properties = new Properties();
        loadConfiguration();
    }

    private void loadConfiguration() {
        Dotenv dotenv = null;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            System.err.println("Warning: .env file not found");
        }

        properties.setProperty("db.connection",
                getEnvValue(dotenv, "DB_CONNECTION_STRING", getDbConnection()));
        properties.setProperty("db.name",
                getEnvValue(dotenv, "DB_NAME", "storm_data"));
        properties.setProperty("api.key",
                getEnvValue(dotenv, "GOOGLE_API_KEY", ""));
        properties.setProperty("app.topic",
                getEnvValue(dotenv, "DEFAULT_TOPIC", "Typhoon Yagi"));
        properties.setProperty("app.year",
                getEnvValue(dotenv, "ANALYSIS_YEAR", "2024"));
    }

    private String getEnvValue(Dotenv dotenv, String key, String defaultValue) {
        if (dotenv != null) {
            String value = dotenv.get(key);
            if (value != null) return value;
        }
        String systemValue = System.getenv(key);
        return systemValue != null ? systemValue : defaultValue;
    }


    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String getDbConnection() {
        return "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    }

    public String getDbName() {
        return properties.getProperty("db.name");
    }

    public String getApiKey() {
        return properties.getProperty("api.key");
    }

    public String getDefaultTopic() {
        return properties.getProperty("app.topic");
    }

    public int getAnalysisYear() {
        return Integer.parseInt(properties.getProperty("app.year"));
    }
}