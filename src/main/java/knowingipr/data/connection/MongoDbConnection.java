package knowingipr.data.connection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import knowingipr.data.loader.IDbLoadArgs;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages the source database. (MongoDB)
 * @author Stepan Baratta
 */
public class MongoDbConnection implements SourceDbConnection {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String DEFAULT_DB_NAME = "knowingipr";
    private static final String MONGO_CONFIG_PATH = "mongo-config.cfg";

    private String dbName = DEFAULT_DB_NAME;

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
    public void createTextIndex(String collectionName, String... fields) {
        LOGGER.info("Creating text index on fields : " + Arrays.toString(fields));

        MongoCollection<Document> collection = getCollection(collectionName);
        collection.createIndex(Indexes.compoundIndex(getFieldsAsBson(fields)));

        LOGGER.info("Text index created");

    }

    private List<Bson> getFieldsAsBson(String... fields) {
        List<Bson> bsonList = new ArrayList<>();
        for (String field : fields) {
            bsonList.add(Indexes.text(field));
        }

        return bsonList;
    }

    @Override
    public void createIndexes(String collectionName, String... fields) {
        MongoCollection<Document> collection = getCollection(collectionName);
        for (String field : fields) {
            LOGGER.info("Creating index on field : " + field);

            try {
                collection.createIndex(Indexes.ascending(field), new IndexOptions().collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build()));
            } catch (Exception e) {
                LOGGER.severe("Index on field " + field + " could not be created: " + e.getMessage());
            }

            LOGGER.info("Index on field " + field + " created.");
        }
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
