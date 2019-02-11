package knowingipr.dam.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.logging.Logger;

/**
 * Manages the source database. (MongoDB)
 */
public class SourceDbManager {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String DB_NAME = "diploma";

    private MongoDatabase mongoDatabase;

    public SourceDbManager() {
        connect();
    }

    public void connect() {
        LOGGER.info("Connecting to the MongoDB database " + DB_NAME);
        MongoClient mongoClient = MongoClients.create();
        mongoDatabase = mongoClient.getDatabase(DB_NAME);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
