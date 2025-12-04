package project.app.humanelogistics.factory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import project.app.humanelogistics.config.AppConfig;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.db.MongoMediaRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryFactory implements AutoCloseable {
    private final String connectionString;
    private final String databaseName;
    private final MongoClient mongoClient;
    private final Map<String, MediaRepository> repositoryCache;

    public RepositoryFactory(AppConfig config) {
        this.connectionString = config.getDbConnection();
        this.databaseName = config.getDbName();
        this.mongoClient = MongoClients.create(connectionString);
        this.repositoryCache = new ConcurrentHashMap<>();
    }

    public MediaRepository getNewsRepository() {
        return repositoryCache.computeIfAbsent("news",
                k -> new MongoMediaRepository(mongoClient, databaseName, "news"));
    }

    public MediaRepository getSocialPostRepository() {
        return repositoryCache.computeIfAbsent("posts",
                k -> new MongoMediaRepository(mongoClient, databaseName, "posts"));
    }

    @Override
    public void close() {
        repositoryCache.clear();
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
