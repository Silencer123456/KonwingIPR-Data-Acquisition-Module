package knowingipr.data.loader;

import knowingipr.data.utils.DirectoryHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.Document;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the loader of patent data to the database.
 */
public class PatentLoader extends SourceDbLoader {

    //private static final String COLLECTION_NAME = "test";

    private String mappingFilePath;
    private String collectionName;

    public PatentLoader(SourceDbConnection dbConnection, String mappingFilePath, String collectionName) {
        super(dbConnection);

        this.mappingFilePath = mappingFilePath;
        this.collectionName = collectionName;
    }

    // TODO: Maybe remove from parent and make private
    @Override
    public void insertFromFile(File file) throws IOException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        List<Document> docs = parseFileStreaming(file);
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
    public void loadFromDirectory(String dirPath, String[] extensions) throws IOException {
        List<File> files = DirectoryHandler.ListFilesFromDirectory(dirPath, extensions, true);
        for (File file : files) {
            LOGGER.info("Processing " + file.getCanonicalPath());
            insertFromFile(file);
        }
    }

    /**
     * TODO: Move to separate class, so that this class does not depend on the concrete file loading (JSON here)
     * Streams the file and from its contents creates a list of documents to be added to the database.
     *
     * @param file - File to be parsed
     * @return - List of parsed documents to be added to the database. If there is an error parsing
     * the file, an empty list is returned
     */
    private List<Document> parseFileStreaming(File file) throws IOException {
        List<Document> documents = new ArrayList<>();

        JsonFactory factory = new MappingJsonFactory();
        try (JsonParser parser = factory.createParser(file)) {
            JsonToken current = parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                current = parser.nextToken();
                if (fieldName.equals("us-patent-grant")) {
                    if (current == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            JsonNode node = parser.readValueAsTree();
                            preprocessNode(node);
                            documents.add(Document.parse(node.toString()));
                        }
                    }
                }
            }
        } catch (JsonParseException e) {
            LOGGER.warning("Error parsing file " + file.getCanonicalPath());
            return Collections.emptyList();
        }

        return documents;
    }

    // TODO: Read from mapping file, throw mappingexception when error
    private void preprocessNode(JsonNode node) {
        JsonNode mappingRoot;
        try {
            mappingRoot = loadMappingFile().get("uspto");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.severe("Error loading the mapping");
            return;
        }

        String titlePath = mappingRoot.get(MappedFields.TITLE.value).textValue();
        String abstractPath = mappingRoot.get(MappedFields.ABSTRACT.value).textValue();
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        String authorsRootPath = mappingRoot.path(MappedFields.AUTHORS.value).path("array-root").textValue();

        JsonNode titleNode = node.at(titlePath);
        JsonNode abstractNode = node.at(abstractPath);
        StringBuilder abstractText = new StringBuilder();
        if (abstractNode.isArray()) { // Sometimes the abstract is separated by new line into array. Do not know why
            for (JsonNode curNode : abstractNode) {
                abstractText.append(curNode.textValue());
            }
        } else {
            abstractText.append(abstractNode.textValue());
        }

        JsonNode yearNode = node.at(yearPath);
        JsonNode authorsNode = node.at(authorsRootPath);

        String firstNamePath = mappingRoot.path(MappedFields.AUTHORS.value).path("aggregate").path("first-name").textValue();
        String lastNamePath = mappingRoot.path(MappedFields.AUTHORS.value).path("aggregate").path("last-name").textValue();

        List<String> authors = new ArrayList<>();
        JsonNode firstNameNode = authorsNode.at(firstNamePath);
        JsonNode lastNameNode = authorsNode.at(lastNamePath);
        if (firstNameNode.textValue() != null && lastNameNode.textValue() != null) {
            if (authorsNode.isArray()) {
                for (JsonNode authorNode : authorsNode) {
                    authors.add(firstNameNode.textValue() + " " + lastNameNode.textValue());
                    firstNameNode = authorNode.at(firstNamePath);
                    lastNameNode = authorNode.at(lastNamePath);
                }
            } else {
                authors.add(firstNameNode.textValue() + " " + lastNameNode.textValue());
            }
        }

        ((ObjectNode)node).put(MappedFields.TITLE.value, titleNode.textValue());

        if (!abstractText.toString().equals("null")) {
            ((ObjectNode)node).put(MappedFields.ABSTRACT.value, abstractText.toString());
        }
        ((ObjectNode)node).put(MappedFields.YEAR.value, yearNode.toString().substring(0, 4));
        ((ObjectNode)node).put("data-source", "uspto");

        //ArrayNode authorsArray = new ArrayNode(factory);
        if (!authors.isEmpty()) {
            ArrayNode authorsArray = ((ObjectNode)node).putArray(MappedFields.AUTHORS.value);

            JsonNodeFactory f = JsonNodeFactory.instance;
            for (String authorName : authors) {
                ObjectNode att = f.objectNode();

                att.put("name", authorName);
                authorsArray.add(att);
            }
        }
    }

    private JsonNode loadMappingFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream input = new FileInputStream(mappingFilePath);
        return objectMapper.readTree(input);

    }
}
