package knowingipr.data.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import knowingipr.data.exception.MappingException;
import knowingipr.data.loader.MappedFields;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class handles manipulation with json files using external mapping configuration.
 */
public class JsonMappingTransformer {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Extracts all the array values from the paths specified in the mapping.
     * If the node is not an array, it extracts present fields.
     *
     * @param node        - The target node from which to extract the values
     * @param mappingNode - Node in the mapping file to process
     * @return - List of values of the
     * @throws MappingException
     */
    public static List<String> extractArrayFromMapping(JsonNode node, JsonNode mappingNode) throws MappingException {
        String arrayRootPath = mappingNode.path("array-root").textValue();
        JsonNode arrayNode = node.at(arrayRootPath);

        // In case the field consists of multiple fields, e.g. Authors first name and last name
        List<String> valueParts = new ArrayList<>();
        JsonNode values = mappingNode.path("values");
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
     *
     * @param valuePartPaths - Paths in the json document to fields I want to merge
     * @param node           - The json node in which I want to search the path
     * @return The merged string value from multiple fields
     */
    private static String getMergedValue(List<String> valuePartPaths, JsonNode node) {
        StringBuilder resultValue = new StringBuilder();
        // Iterate the value parts that need to be merged into one string
        for (String valuePartPath : valuePartPaths) {
            String nodeText = node.at(valuePartPath).textValue();
            if (nodeText != null) {
                resultValue.append(nodeText).append(" ");
            }
        }

        return resultValue.toString().trim();
    }

    /**
     * Puts a list of values to the target json node with the specified
     * field name.
     *
     * @param valuesList - List of values to insert into the target json node
     * @param targetNode - Target json node
     * @param fieldName  - How to name the field in the target json node
     */
    public static void putArrayToNode(List<String> valuesList, JsonNode targetNode, MappedFields arrayName, String fieldName) {
        if (!valuesList.isEmpty()) {
            ArrayNode array = ((ObjectNode) targetNode).putArray(arrayName.value);

            JsonNodeFactory f = JsonNodeFactory.instance;
            for (String authorName : valuesList) {
                ObjectNode att = f.objectNode();

                att.put(fieldName, authorName);
                array.add(att);
            }
        }
    }

    /**
     * Searches the field in the mapping and extracts the path from it. The path
     * is then used to find the node in the json whose value we want to add to the top level.
     *
     * @param mappingRoot      - Root node of the mapping json file
     * @param field            - Field, that we want the path to be extracted from in the mapping
     * @param nodeToPreprocess - Json node that we want to put the value into.
     *                         TODO: Handle deletion of the old record, change method to moveValueFromPathToTopLevel
     */
    public static void putValueFromPath(JsonNode mappingRoot, MappedFields field, JsonNode nodeToPreprocess) {
        String path = mappingRoot.get(field.value).textValue();
        JsonNode searchedNode = nodeToPreprocess.at(path);
        putPair(nodeToPreprocess, field.value, searchedNode.textValue());
    }

    /**
     * Puts a field name with value to the json node
     *
     * @param node  - Node to which we want to add the field name with value
     * @param name  - Name of the field name
     * @param value - Value for the field name
     */
    public static void putPair(JsonNode node, String name, String value) {
        ((ObjectNode) node).put(name, value);
    }

    /**
     * Gets a list of String values from a node specified in the mapping. The mapping node can be an array
     * (have multiple mappings for the same field -- mainly if the data source changes structure over time)
     * @param mappingRoot - Root mapping node
     * @param field - Field in the mapping to look for
     * @param nodeToPreprocess - Node, in which we search mapped fields and add the new extracted fields
     * @return list of String values extracted from the mapping
     * @throws MappingException
     */
    public static List<String> getValuesListFromMappingArray(JsonNode mappingRoot, MappedFields field, JsonNode nodeToPreprocess) throws MappingException {
        JsonNode mappingNode = mappingRoot.path(field.value);
        List<String> valuesList = new ArrayList<>();
        if (mappingNode.isArray()) {
            for (JsonNode node : mappingNode) {
                valuesList = JsonMappingTransformer.extractArrayFromMapping(nodeToPreprocess, node);
                if (!valuesList.isEmpty()) {
                    return valuesList;
                }
            }
        }

        return valuesList;
    }
}
