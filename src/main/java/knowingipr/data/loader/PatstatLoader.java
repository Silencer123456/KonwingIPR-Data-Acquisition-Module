package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.SourceDbConnection;
import knowingipr.data.exception.MappingException;

import java.io.File;
import java.io.IOException;

/**
 * Handles loading of data from PATSTAT to the sources database
 */
public class PatstatLoader extends SourceDbLoader {

    private static final LoaderName SOURCE_NAME = LoaderName.PATSTAT_LOADER;

    private String collectionName;
    private JsonParser jsonParser;

    public PatstatLoader(SourceDbConnection dbConnection, String mappingFile, String collectionName) {
        super(dbConnection, mappingFile, SOURCE_NAME.name);

        this.collectionName = SOURCE_NAME.name;
        jsonParser = new JsonParser();
    }

    @Override
    public void insertFromFile(File file) throws IOException, MappingException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        if (!(dbConnection instanceof MongoDbConnection)) {
            throw new RuntimeException("Wrong connection type. Expected MongoDB connection");
        }

        MongoDbConnection mongoDbConnection = (MongoDbConnection) dbConnection;
        jsonParser.parseJsonByLines(file, mongoDbConnection, collectionName, this, 100000);
        LOGGER.finer("Parsing done");
    }

    @Override
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException, IOException {
        super.preprocessNode(nodeToPreprocess);
    }

    @Override
    public void createIndexes() {
    }
}
