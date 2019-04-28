package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import knowingipr.data.exception.MappingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the loader of MAG data to the database.
 */
public class MagLoader extends SourceDbLoader {
    private String collectionName;

    public MagLoader(SourceDbConnection dbConnection, String mappingFile, String collectionName) {
        super(dbConnection, mappingFile);

        this.collectionName = "test";
    }

    @Override
    public void insertFromFile(File file) throws IOException, MappingException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        parseJsonByLines(file);
        LOGGER.finer("Parsing done");
    }

    /**
     * Reads a JSON file line by line and adds them to the MongoDB collection.
     * Used only if the source JSON file contains one document per line.
     * The files are large, parse by 10 000 files increment
     * @param file - File to be loaded to the db
     */
    private void parseJsonByLines(File file) throws IOException {
        List<Document> docs = new ArrayList<>();
        try(LineIterator fileContents = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name())) {
            while(fileContents.hasNext()) {
                Document doc = Document.parse( fileContents.nextLine());
                docs.add(doc);
                if (docs.size() == 10000) {
                    IDbLoadArgs loadArgs;
                    if (dbConnection instanceof MongoDbConnection) {
                        loadArgs = new MongoDbLoadArgs(collectionName, docs);
                    } else {
                        LOGGER.severe("Unknown connection specified. Exiting now.");
                        throw new RuntimeException();
                    }

                    dbConnection.insert(loadArgs);
                    docs.clear();
                }
            }

        } catch (IOException e) {
            LOGGER.warning("Error parsing file " + file.getCanonicalPath());
        }
    }

    // MAG source does not need preprocessing, it is in an appropriate structure
    @Override
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException, IOException {
    }
}
