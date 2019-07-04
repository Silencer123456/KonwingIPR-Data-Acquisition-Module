package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.SourceDbConnection;
import knowingipr.data.exception.MappingException;
import knowingipr.data.mapper.JsonMappingTransformer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Implementation of the loader of Springer LOD data.
 */
public class SpringerLoader extends SourceDbLoader {

    private final String SOURCE_NAME = "springer";

    private JsonParser jsonParser;

    private String collectionName;

    public SpringerLoader(SourceDbConnection dbConnection, String mappingFilePath, String collectionName) {
        super(dbConnection, mappingFilePath);

        this.collectionName = "test2";
        jsonParser = new JsonParser();
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

    /**
     * Preprocesses a json node, so that it contains all the necessary fields in the top level
     * in the json hierarchy.
     *
     * @param nodeToPreprocess - Node to preprocess.
     * @throws MappingException - if there is an error in the mapping file
     */
    @Override
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException, IOException {
        JsonNode mappingRoot = loadMappingFile().get(SOURCE_NAME);
        if (mappingRoot == null) {
            String err = "Mapping file does not contain node " + SOURCE_NAME;
            LOGGER.severe(err);
            throw new MappingException(err);
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

        // Publisher
        JsonMappingTransformer.putValueFromPath(mappingRoot, MappedFields.PUBLISHER, nodeToPreprocess);

        // Authors array
        ArrayNode authorsArray = JsonMappingTransformer.getNodesArrayWithOptions(mappingRoot, MappedFields.AUTHORS, nodeToPreprocess);
        JsonMappingTransformer.putJsonArray(nodeToPreprocess, authorsArray, "authors");

        // Affiliation
        List<String> affiliationList = JsonMappingTransformer.getValuesListFromArray(mappingRoot, MappedFields.AFFILIATION, nodeToPreprocess);
        JsonMappingTransformer.putArrayToNode(affiliationList, nodeToPreprocess, MappedFields.AFFILIATION, "name");

        // Title
        JsonMappingTransformer.putValueFromPath(mappingRoot, MappedFields.TITLE, nodeToPreprocess);

        // Url
        JsonMappingTransformer.putValueFromPath(mappingRoot, MappedFields.URL, nodeToPreprocess);

        // Year
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        String date = nodeToPreprocess.at(yearPath).textValue();
        if (date != null) {
            JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.YEAR.value, date.substring(0, 4));
        }

        // Data Source
        JsonMappingTransformer.putPair(nodeToPreprocess, "dataSource", SOURCE_NAME);
    }
}
