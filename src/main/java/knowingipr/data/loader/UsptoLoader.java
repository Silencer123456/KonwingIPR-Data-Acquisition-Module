package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import knowingipr.data.connection.MongoDbConnection;
import knowingipr.data.connection.MongoDbLoadArgs;
import knowingipr.data.connection.SourceDbConnection;
import knowingipr.data.exception.MappingException;
import knowingipr.data.mapper.JsonMappingTransformer;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of the loader of USPTO patent data to the database.
 */
public class UsptoLoader extends SourceDbLoader {

    private static final String SOURCE_NAME = "uspto";

    private JsonParser jsonParser;

    private String collectionName;

    public UsptoLoader(SourceDbConnection dbConnection, String mappingFilePath, String collectionName) {
        super(dbConnection, mappingFilePath);

        this.collectionName = collectionName;
        jsonParser = new JsonParser();
    }

    // TODO: Decide if parse or load to memory by the size of the file
    @Override
    public void insertFromFile(File file) throws IOException, MappingException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        List<Document> docs = jsonParser.parseJsonStreaming(file, this, "us-patent-grant");
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

        // Patent number
        String patentIdPath = mappingRoot.path(MappedFields.ID.value).textValue();
        String patNumberId = nodeToPreprocess.at(patentIdPath).textValue();
        JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.ID.value, extractPatentNumber(patNumberId));

        // Authors
        ArrayNode authorsArray = JsonMappingTransformer.getNodesArrayMultipleOptions(mappingRoot, MappedFields.AUTHORS, nodeToPreprocess);
        JsonMappingTransformer.putJsonArray(nodeToPreprocess, authorsArray, MappedFields.AUTHORS.value);

        // Owners
        ArrayNode ownersArray = JsonMappingTransformer.getNodesArrayMultipleOptions(mappingRoot, MappedFields.OWNERS, nodeToPreprocess);
        JsonMappingTransformer.putJsonArray(nodeToPreprocess, ownersArray, MappedFields.OWNERS.value);

        // Title
        JsonMappingTransformer.putValueFromPath(mappingRoot, MappedFields.TITLE, nodeToPreprocess);

        // Year
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        String format = mappingRoot.path(MappedFields.YEAR.value).path("format").textValue();
        JsonNode yearNode = nodeToPreprocess.at(yearPath);
        JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.YEAR.value, yearNode.toString().substring(0, 4));

        // Date
        LocalDate date = extractDate(yearNode.toString(), format);
        if (date != null) {
            JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.DATE.value, date.toString());
        }

        // Data Source
        JsonMappingTransformer.putPair(nodeToPreprocess, "dataSource", SOURCE_NAME);
    }

    /**
     * db.patent.createIndex(
     * {
     * title: "text",
     * abstract: "text",
     * "authors.name": "text",
     * "owners.name": "text"
     * }
     * )
     */
    @Override
    public void createIndexes() {
        dbConnection.createTextIndex(collectionName, MappedFields.TITLE.value, MappedFields.ABSTRACT.value,
                "authors.name", "owners.name");

        dbConnection.createIndexes(collectionName, MappedFields.ID.value, MappedFields.TITLE.value);
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

    /**
     * Extracts a patent number from a string.
     * In USPTO, patent number field can look like this: USPP027521-20170103.XML
     * In this case, extracted patent number would be : USPP27521
     *
     * @return patent number
     */
    private String extractPatentNumber(String patentId) {
        StringBuilder patentNumber = new StringBuilder(patentId.split("-")[0]);
        patentNumber.deleteCharAt(patentNumber.indexOf("0"));

        return patentNumber.toString();
    }
}
