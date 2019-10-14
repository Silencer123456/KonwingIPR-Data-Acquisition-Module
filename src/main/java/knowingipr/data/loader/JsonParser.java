package knowingipr.data.loader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.MongoDbLoadArgs;
import knowingipr.data.exception.MappingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class JsonParser {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Streams the file and from its contents creates a list of Mongo documents to be added to the Mongo database.
     *
     * @param file - File to be parsed
     * @param loader - The callback loader to call for node preprocessing
     * @param arrayName - Name of the array element to search
     * @return - List of parsed documents to be added to the database. If there is an error parsing
     * the file, an empty list is returned
     */
    public List<Document> parseJsonStreaming(File file, SourceDbLoader loader, String arrayName) throws IOException, MappingException {
        List<Document> documents = new ArrayList<>();

        JsonFactory factory = new MappingJsonFactory();
        try (com.fasterxml.jackson.core.JsonParser parser = factory.createParser(file)) {
            JsonToken current = parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                current = parser.nextToken();
                if (fieldName.equals(arrayName)) {
                    if (current == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            JsonNode node = parser.readValueAsTree();
                            loader.preprocessNode(node);
                            documents.add(Document.parse(node.toString()));
                        }
                    }
                }
            }
        } catch (JsonParseException e) {
            LOGGER.warning("Error parsing file " + file.getCanonicalPath());
            e.printStackTrace();
            return Collections.emptyList();
        }

        return documents;
    }

    /**
     * Reads a JSON file line by line and immediately inserts the created documents to the Mongo collection.
     * Used only if the source JSON file contains one document per line.
     * If the file is large, it is split into 10000 documents chunks.
     *
     * @param file - File to be loaded to the db
     */
    public void parseJsonByLines(File file, MongoDbConnection dbConnection, String collectionName, SourceDbLoader loader) throws IOException, org.bson.json.JsonParseException {
        ObjectMapper mapper = new ObjectMapper();

        MongoDbLoadArgs loadArgs = new MongoDbLoadArgs(collectionName);

        List<Document> docs = new ArrayList<>();
        try (LineIterator fileContents = FileUtils.lineIterator(file, StandardCharsets.UTF_8.name())) {
            while (fileContents.hasNext()) {
                JsonNode node = mapper.readTree(fileContents.nextLine());
                loader.preprocessNode(node);

                Document doc = Document.parse(node.toString());
                docs.add(doc);
                if (docs.size() == 10000) {
                    loadArgs.setDocuments(docs);

                    dbConnection.insert(loadArgs);
                    docs.clear();
                }
            }
            // Add any remaining documents
            if (!docs.isEmpty()) {
                loadArgs.setDocuments(docs);
                dbConnection.insert(loadArgs);
            }

        } catch (MappingException e) {
            LOGGER.warning("Mapping error while preprocessing a node.");
            e.printStackTrace();
        }
    }
}