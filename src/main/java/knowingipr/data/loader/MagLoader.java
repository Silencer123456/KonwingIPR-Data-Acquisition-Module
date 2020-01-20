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

    private static final String SOURCE_NAME = "mag";

    private JsonParser jsonParser;

    public MagLoader(SourceDbConnection dbConnection, String mappingFile, String collectionName) {
        super(dbConnection, mappingFile, SOURCE_NAME);

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
        jsonParser.parseJsonByLines(file, mongoDbConnection, collectionName, this, 10000);
        LOGGER.finer("Parsing done");
    }

    // MAG source does not need preprocessing, it is in an appropriate structure
    @Override
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException, IOException {
    }

    @Override
    public void createIndexes() {
        dbConnection.createTextIndex(collectionName, MappedFields.TITLE.value, MappedFields.ABSTRACT.value,
                "authors.name", MappedFields.FOS.value, MappedFields.KEYWORDS.value);

        dbConnection.createIndexes(collectionName, MappedFields.FOS.value, MappedFields.KEYWORDS.value, MappedFields.LANG.value, MappedFields.PUBLISHER.value, MappedFields.VENUE.value);
    }
}
