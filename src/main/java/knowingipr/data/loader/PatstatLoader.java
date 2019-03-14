package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import knowingipr.data.exception.MappingException;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PatstatLoader extends SourceDbLoader {

    private String collectionName;
    JsonParser jsonParser;

    public PatstatLoader(SourceDbConnection dbConnection, String mappingFile, String collectionName) {
        super(dbConnection, mappingFile);

        this.collectionName = collectionName;
        jsonParser = new JsonParser();
    }

    @Override
    public void insertFromFile(File file) throws IOException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        List<Document> docs = jsonParser.parseFileStreaming(file, this, "PATSTAT");
        LOGGER.finer("Parsing done");

        if (docs.isEmpty()) {
            LOGGER.warning("List is empty, skipping...");
            return;
        }

        IDbLoadArgs loadArgs;
        if (dbConnection instanceof MongoDbConnection) {
            loadArgs = new MongoDbLoadArgs(collectionName, docs);
        } else {
            LOGGER.severe("Unknown connection specified. Exiting now.");
            throw new RuntimeException();
        }

        dbConnection.insert(loadArgs);
    }

    @Override
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException {

    }
}
