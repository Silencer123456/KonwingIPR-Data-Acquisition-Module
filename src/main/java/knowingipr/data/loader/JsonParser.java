package knowingipr.data.loader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import knowingipr.data.exception.MappingException;
import org.bson.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class JsonParser {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * TODO: Move to separate class, so that this class does not depend on the concrete file loading (JSON here)
     * Streams the file and from its contents creates a list of documents to be added to the database.
     *
     * @param file - File to be parsed
     * @param loader - The callback loader to call for node preprocessing
     * @param arrayName - Name of the array element to search
     * @return - List of parsed documents to be added to the database. If there is an error parsing
     * the file, an empty list is returned
     */
    public List<Document> parseFileStreaming(File file, SourceDbLoader loader, String arrayName) throws IOException, MappingException {
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
}
