package knowingipr.data.connection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import knowingipr.data.loader.IDbLoadArgs;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Manages the source database. (MongoDB)
 * @author Stepan Baratta
 */
public class MongoDbConnection implements SourceDbConnection {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //private static final String DB_NAME = "sources";

    private static final String MONGO_CONFIG_PATH = "mongo-config.cfg";

    private String dbName = "diploma";

    private MongoDatabase mongoDatabase;

    public MongoDbConnection() {
        loadConfig();
        connect();
    }

    /**
     * Loads configuration of the MongoDB connection
     */
    private void loadConfig() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(MONGO_CONFIG_PATH));
            dbName = prop.getProperty("db_name");
        } catch (IOException e) {
            LOGGER.severe("Unable to find " + MONGO_CONFIG_PATH + " file. Using default " +
                    "database: " + dbName);
            e.printStackTrace();
        }
    }

    public void connect() {
        LOGGER.info("Connecting to the MongoDB database " + dbName);
        MongoClient mongoClient = MongoClients.create();
        mongoDatabase = mongoClient.getDatabase(dbName);

        //MongoClient client = MongoClients.create("mongodb://dunabe:dkd8dD-d23dwdw@students/?authSource=dunabe");
        //mongoDatabase = client.getDatabase(DB_NAME);
    }

    /**
     * The collection is emptied after the insert to the Mongo database
     *
     * @param loadArgs - The data structure to be loaded into the target database
     */
    @Override
    public void insert(IDbLoadArgs loadArgs) {
        if (!(loadArgs instanceof MongoDbLoadArgs)) {
            LOGGER.severe("The data for loading is not correct. Expected instance of " + MongoDbLoadArgs.class.getName() + ". Exiting now.");
            throw new RuntimeException();
        }

        if (mongoDatabase == null) {
            connect();
        }

        InsertManyOptions options = new InsertManyOptions();
        options.ordered(false);

        MongoDbLoadArgs mongoArgs = (MongoDbLoadArgs) loadArgs;

        MongoCollection<Document> collection = getCollection(mongoArgs.getCollectionName());
        LOGGER.finer("Beginning insert");
        collection.insertMany(mongoArgs.getDocuments(), options);
        LOGGER.finer("Inserting done");

        mongoArgs.setDocuments(Collections.emptyList());
    }

    @Override
    public void disconnect() {
    }

    public MongoCollection<Document> getCollection(String collName) {
        return mongoDatabase.getCollection(collName);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
