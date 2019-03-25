package knowingipr.data.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import knowingipr.data.exception.MappingException;
import knowingipr.data.mapper.JsonMappingTransformer;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Implementation of the loader of patent data to the database.
 */
public class PatentLoader extends SourceDbLoader {

    JsonParser jsonParser;

    private String collectionName;

    public PatentLoader(SourceDbConnection dbConnection, String mappingFilePath, String collectionName) {
        super(dbConnection, mappingFilePath);

        this.collectionName = collectionName;
        jsonParser = new JsonParser();
    }

    // TODO: Maybe remove from parent and make private
    @Override
    public void insertFromFile(File file) throws IOException {
        LOGGER.finer("Parsing file " + file.getCanonicalPath());
        List<Document> docs = jsonParser.parseFileStreaming(file, this, "us-patent-grant");
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
    public void preprocessNode(JsonNode nodeToPreprocess) throws MappingException {
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

        // Patent number
        String patentIdPath = mappingRoot.path(MappedFields.ID.value).textValue();
        String patNumberId = nodeToPreprocess.at(patentIdPath).textValue();
        JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.ID.value, extractPatentNumber(patNumberId));

        // Authors
        List<String> authorsList = JsonMappingTransformer.getValuesListFromMappingArray(mappingRoot, MappedFields.AUTHORS, nodeToPreprocess);
        JsonMappingTransformer.putArrayToNode(authorsList, nodeToPreprocess, MappedFields.AUTHORS, "name");

        // Owners
        List<String> ownersList = JsonMappingTransformer.getValuesListFromMappingArray(mappingRoot, MappedFields.OWNERS, nodeToPreprocess);
        JsonMappingTransformer.putArrayToNode(ownersList, nodeToPreprocess, MappedFields.OWNERS, "name");

        // Title
        JsonMappingTransformer.putValueFromPath(mappingRoot, MappedFields.TITLE, nodeToPreprocess);

        // Year
        String yearPath = mappingRoot.path(MappedFields.YEAR.value).path("path").textValue();
        JsonNode yearNode = nodeToPreprocess.at(yearPath);
        JsonMappingTransformer.putPair(nodeToPreprocess, MappedFields.YEAR.value, yearNode.toString().substring(0, 4));
        //((ObjectNode) nodeToPreprocess).put(MappedFields.YEAR.value, yearNode.toString().substring(0, 4));

        // Data Source
        JsonMappingTransformer.putPair(nodeToPreprocess, "dataSource", "uspto");
    }

    private JsonNode loadMappingFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream input = new FileInputStream(mappingFilePath);
        return objectMapper.readTree(input);
    }

    /**
     * Extracts a patent number from a string.
     * In USPTO, patent number field can look like this: USPP027521-20170103.XML
     * In this case, extracted patent number would be : USPP27521
     * @return patent number
     */
    private String extractPatentNumber(String patentId) {
        StringBuilder patentNumber = new StringBuilder(patentId.split("-")[0]);
        patentNumber.deleteCharAt(patentNumber.indexOf("0"));

        return patentNumber.toString();
    }
}
