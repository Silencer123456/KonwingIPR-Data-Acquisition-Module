package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.SourceDbConnection;
import knowingipr.data.exception.MappingException;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of the loader of MAG data to the database.
 */
public class MagLoader extends SourceDbLoader {
    private String collectionName;

    private JsonParser jsonParser;

    public MagLoader(SourceDbConnection dbConnection, String mappingFile, String collectionName) {
        super(dbConnection, mappingFile);

        this.collectionName = collectionName;

        this.jsonParser = new JsonParser();
    }

    @Override
    public void insertFromFile(File file) throws IOException, MappingException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        if (!(dbConnection instanceof MongoDbConnection)) {
            throw new RuntimeException("Wrong connection type. Expected MongoDB connection");
        }

        MongoDbConnection mongoDbConnection = (MongoDbConnection) dbConnection;
        jsonParser.parseJsonByLines(file, mongoDbConnection, collectionName, this);
        LOGGER.finer("Parsing done");
    }

    // MAG source does not need preprocessing, it is in an appropriate structure
    @Override
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException, IOException {
    }
}
