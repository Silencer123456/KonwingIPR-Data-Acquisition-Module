package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.SourceDbConnection;
import knowingipr.data.exception.MappingException;
import knowingipr.data.mapper.JsonMappingTransformer;
import knowingipr.data.utils.LoaderUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of the loader of Springer LOD data.
 */
public class SpringerLoader extends SourceDbLoader {

    private static final String SOURCE_NAME = "springer";

    private JsonParser jsonParser;

    private String collectionName;

    public SpringerLoader(SourceDbConnection dbConnection, String mappingFilePath, String collectionName) {
        super(dbConnection, mappingFilePath, SOURCE_NAME);

        this.collectionName = SOURCE_NAME;
        jsonParser = new JsonParser();
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
        JsonMappingTransformer.moveValueFromPathToTopLevel(mappingRoot, MappedFields.PUBLISHER, nodeToPreprocess, false);

        // Authors array
        ArrayNode authorsArray = JsonMappingTransformer.getNodesArrayMultipleOptions(mappingRoot, MappedFields.AUTHORS, nodeToPreprocess);
        JsonMappingTransformer.putJsonArray(nodeToPreprocess, authorsArray, "authors");

        // Affiliation
        List<String> affiliationList = JsonMappingTransformer.getValuesListFromArray(mappingRoot, MappedFields.AFFILIATION, nodeToPreprocess);
        if (affiliationList.contains("null")) {
            System.out.println("test");
        }
        JsonMappingTransformer.putArrayToNode(affiliationList, nodeToPreprocess, MappedFields.AFFILIATION, "name");

        // Title
        JsonMappingTransformer.moveValueFromPathToTopLevel(mappingRoot, MappedFields.TITLE, nodeToPreprocess, true);

        // Url
        JsonMappingTransformer.moveValueFromPathToTopLevel(mappingRoot, MappedFields.URL, nodeToPreprocess, true);

        // DOI
        putDoi(nodeToPreprocess);

        // Language
        JsonMappingTransformer.moveArrayFromPathToTopLevel(mappingRoot, MappedFields.LANG, nodeToPreprocess, true);

        // Year
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        String dateStr = nodeToPreprocess.at(yearPath).textValue();

        if (dateStr == null) dateStr = "";

        String format = mappingRoot.path(MappedFields.YEAR.value).path("format").textValue();
        if (format == null) throw new MappingException("Mapping field format does not exist");

        LocalDate date = LoaderUtils.extractDate(dateStr, format);

        if (date == null && dateStr.length() >= 4) {
            JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.YEAR.value, dateStr.substring(0, 4));
        }
        if (date != null) {
            JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.DATE.value, date.toString());
        }

        super.preprocessNode(nodeToPreprocess);
    }

    private void putDoi(JsonNode nodeToPreprocess) {
        JsonNode productIdNode = nodeToPreprocess.get("productId");
        if (!productIdNode.isArray()) {
            return;
        }

        for (JsonNode node : productIdNode) {
            if (node.get("name").textValue().equals("doi")) {

                JsonNode doiValueNode = node.get("value");
                if (!doiValueNode.isArray() && doiValueNode.size() < 1) return;

                ((ObjectNode) nodeToPreprocess).set("doi", node.get("value").get(0));
            }
        }
    }

    @Override
    public void createIndexes() {

    }

    /**
     * Extracts date from node according to the specified format
     *
     * @param dateString - Json node from which to extract the date
     * @param format     - Format of the date
     * @return Parsed Date instance
     */
    private LocalDate extractDate(String dateString, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDate.parse(dateString, formatter);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
