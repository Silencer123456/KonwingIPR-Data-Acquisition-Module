package knowingipr.data.loader;

import knowingipr.data.exception.MappingException;
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
     * @param node - Node to preprocess.
     * @throws MappingException - if there is an error in the mapping file
     */
    private void preprocessNode(JsonNode node) throws MappingException {
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
        JsonNode abstractNode = node.at(abstractPath);
        StringBuilder abstractText = new StringBuilder();
        if (abstractNode.isArray()) { // Sometimes the abstract is separated by new line into array. Do not know why
            for (JsonNode curNode : abstractNode) {
                abstractText.append(curNode.textValue());
            }
        } else {
            abstractText.append(abstractNode.textValue());
        }

        if (!abstractText.toString().equals("null")) {
            ((ObjectNode)node).put(MappedFields.ABSTRACT.value, abstractText.toString());
        }

        // Authors
        List<String> authorsList = extractArrayFromMapping(node, mappingRoot, MappedFields.AUTHORS);
        putArrayToNode(authorsList, node, MappedFields.AUTHORS,"name");

        // Owners
        List<String> ownersList = extractArrayFromMapping(node, mappingRoot, MappedFields.OWNERS);
        putArrayToNode(ownersList, node, MappedFields.OWNERS,"name");

        // Title
        String titlePath = mappingRoot.get(MappedFields.TITLE.value).textValue();
        JsonNode titleNode = node.at(titlePath);
        ((ObjectNode)node).put(MappedFields.TITLE.value, titleNode.textValue());

        // Year
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        JsonNode yearNode = node.at(yearPath);
        ((ObjectNode)node).put(MappedFields.YEAR.value, yearNode.toString().substring(0, 4));

        // Data Source
        ((ObjectNode)node).put("dataSource", "uspto");
    }

    /**
     * Puts a list of values to the target json node with the specified
     * field name.
     * @param valuesList - List of values to insert into the target json node
     * @param targetNode - Target json node
     * @param fieldName - How to name the field in the target json node
     */
    private void putArrayToNode(List<String> valuesList, JsonNode targetNode, MappedFields arrayName, String fieldName) {
        if (!valuesList.isEmpty()) {
            ArrayNode array = ((ObjectNode)targetNode).putArray(arrayName.value);

            JsonNodeFactory f = JsonNodeFactory.instance;
            for (String authorName : valuesList) {
                ObjectNode att = f.objectNode();

                att.put(fieldName, authorName);
                array.add(att);
            }
        }
    }

    /**
     * Extracts all the array values from the paths specified in the mapping.
     * If the node is not an array, it extracts present fields.
     * @param node - The target node from which to extract the values
     * @param mappingRoot - The root node of the mapping file
     * @param field - The field in the mapping file to use
     * @return - List of values of the
     * @throws MappingException
     */
    private List<String> extractArrayFromMapping(JsonNode node, JsonNode mappingRoot, MappedFields field) throws MappingException {
        String arrayRootPath = mappingRoot.path(field.value).path("array-root").textValue();
        JsonNode arrayNode = node.at(arrayRootPath);

        // In case the field consists of multiple fields, e.g. Authors first name and last name
        List<String> valueParts = new ArrayList<>();
        JsonNode values = mappingRoot.path(field.value).path("values");
        if (!values.isArray()) {
            LOGGER.severe("The field values must be an array!");
            throw new MappingException("The field values must be an array!");
        }

        for (JsonNode valueNode : values) {
            valueParts.add(valueNode.textValue());
        }

        // The final list of values from the array
        List<String> arrayValues = new ArrayList<>();

        if (arrayNode.isArray()) {
            // Iterate elements of the array
            for (JsonNode arrayElement : arrayNode) {
                String value = getMergedValue(valueParts, arrayElement);
                if (!value.isEmpty()) {
                    arrayValues.add(value);
                }
            }
        } else {
            String value = getMergedValue(valueParts, arrayNode);
            if (!value.isEmpty()) {
                arrayValues.add(value);
            }
        }

        return arrayValues;
    }

    /**
     * Returns a merged string value from multiple fields.
     * @param valuePartPaths - Paths in the json document to fields I want to merge
     * @param node - The json node in which I want to search the path
     * @return The merged string value from multiple fields
     */
    private String getMergedValue(List<String> valuePartPaths, JsonNode node) {
        StringBuilder resultValue = new StringBuilder();
        // Iterate the value parts that need to be merged into one string
        for (String valuePartPath : valuePartPaths) {
            resultValue.append(node.at(valuePartPath).textValue()).append(" ");
        }

        return resultValue.toString().trim();
    }

    private JsonNode loadMappingFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream input = new FileInputStream(mappingFilePath);
        return objectMapper.readTree(input);

    }
}
