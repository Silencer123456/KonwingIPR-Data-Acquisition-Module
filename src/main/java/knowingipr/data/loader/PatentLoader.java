package knowingipr.data.loader;

import knowingipr.data.exception.MappingException;
import knowingipr.data.mapper.JsonMappingTransformer;
import knowingipr.data.utils.DirectoryHandler;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String COLLECTION_NAME = "test";

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
            loadArgs = new MongoDbLoadArgs(COLLECTION_NAME, docs);
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
            e.printStackTrace();
            return Collections.emptyList();
        } catch (MappingException e) {
            LOGGER.warning("Mapping file error: " + e.getMessage());
            return Collections.emptyList();
        }

        return documents;
    }

    /**
     * Preprocesses a json node, so that it contains all the necessary fields in the top level
     * in the json hierarchy.
     *
     * @param nodeToPreprocess - Node to preprocess.
     * @throws MappingException - if there is an error in the mapping file
     */
    private void preprocessNode(JsonNode nodeToPreprocess) throws MappingException {
        JsonNode mappingRoot;
        try {
            mappingRoot = loadMappingFile().get("uspto");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.severe("Error loading the mapping");
            return;
        }

        // Abstract
        String abstractPath = mappingRoot.get(MappedFields.ABSTRACT.value).textValue();
        JsonNode abstractNode = nodeToPreprocess.at(abstractPath);
        StringBuilder abstractText = new StringBuilder();
        if (abstractNode.isArray()) { // Sometimes the abstract is separated by new line into array. Do not know why
            for (JsonNode curNode : abstractNode) {
                abstractText.append(curNode.textValue());
            }
        } else {
            abstractText.append(abstractNode.textValue());
        }

        if (!abstractText.toString().equals("null")) {
            JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.ABSTRACT.value, abstractText.toString());
        }

        // Authors
        List<String> authorsList = JsonMappingTransformer.extractArrayFromMapping(nodeToPreprocess, mappingRoot, MappedFields.AUTHORS);
        JsonMappingTransformer.putArrayToNode(authorsList, nodeToPreprocess, MappedFields.AUTHORS, "name");

        // Owners
        List<String> ownersList = JsonMappingTransformer.extractArrayFromMapping(nodeToPreprocess, mappingRoot, MappedFields.OWNERS);
        JsonMappingTransformer.putArrayToNode(ownersList, nodeToPreprocess, MappedFields.OWNERS, "name");

        // Title
        JsonMappingTransformer.putValueFromPath(mappingRoot, MappedFields.TITLE, nodeToPreprocess);

        // Year
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        JsonNode yearNode = nodeToPreprocess.at(yearPath);
        ((ObjectNode) nodeToPreprocess).put(MappedFields.YEAR.value, yearNode.toString().substring(0, 4));

        // Data Source
        JsonMappingTransformer.putPair(nodeToPreprocess, "dataSource", "uspto");
    }

    private JsonNode loadMappingFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream input = new FileInputStream(mappingFilePath);
        return objectMapper.readTree(input);

    }
}
