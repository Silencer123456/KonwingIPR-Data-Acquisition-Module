package knowingipr.data.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import knowingipr.data.exception.MappingException;
import knowingipr.data.loader.MappedFields;

import java.util.ArrayList;
import java.util.Iterator;
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

        JsonNode values = mappingNode.path("values");
        if (!values.isArray()) {
            LOGGER.severe("The field values must be an array!");
            throw new MappingException("The field values must be an array!");
        }

        return getMergedValues(values, arrayNode);
    }

    /**
     * Returns a list of merged strings values from array node with paths.
     *
     * @param valuePaths - List of paths to Json nodes containing values
     * @param node   - The json node array in which we want to search the path
     * @return The merged list of strings values from multiple fields
     */
    private static List<String> getMergedValues(JsonNode valuePaths, JsonNode node) {
        List<String> mergedValues = new ArrayList<>();

        // Iterate elements of the array TODO: check if it is array
        for (JsonNode arrayElement : node) {
            getMergedValue(valuePaths, arrayElement);

            String s = getMergedValue(valuePaths, arrayElement);
            if (!s.isEmpty()) {
                mergedValues.add(s);
            }
        }

        return mergedValues;
    }

    /**
     * Extracts single merged value from the list of paths
     *
     * @param valuePaths - List of paths to Json nodes containing values
     * @param node       - The json node in which we want to search the path
     * @return Single merged value
     */
    private static String getMergedValue(JsonNode valuePaths, JsonNode node) {
        List<String> valuePartsPaths = new ArrayList<>();
        for (JsonNode valueNode : valuePaths) {
            valuePartsPaths.add(valueNode.textValue());
        }

        StringBuilder resultValue = new StringBuilder();
        // Iterate the value parts that need to be merged into one string
        for (String valuePartPath : valuePartsPaths) {
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
        if (searchedNode.textValue() == null || searchedNode.textValue().isEmpty()) {
            LOGGER.warning("Missing value for field " + field.value);
            System.out.println("Missing value for field " + field.value);
        }
        putPair(nodeToPreprocess, field.value, searchedNode.textValue());

        //((ObjectNode) nodeToPreprocess).remove(path);
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

    static int empty = 0; // TMP
    static int full = 0; // TMP

    /**
     * Wraps the putArray function which puts an array to a json node
     * with the specified name
     *
     * @param node  - Node to which to put the array
     * @param array - Array to put to the node
     */
    public static void putJsonArray(JsonNode node, ArrayNode array, String arrayName) {
        if (array == null) {
            LOGGER.warning("Array " + arrayName + " is empty. Empty: " + empty + ", full: " + full);
            empty++;
            return;
        }
        full++;
        ((ObjectNode) node).putArray(arrayName).addAll(array);
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

    public static List<String> getValuesListFromArray(JsonNode mappingRoot, MappedFields field, JsonNode nodeToPreprocess) throws MappingException {
        JsonNode mappingNode = mappingRoot.path(field.value);
        List<String> valuesList = new ArrayList<>();
        if (mappingNode.isArray()) {
            for (JsonNode node : mappingNode) {
                valuesList = JsonMappingTransformer.extractArrayValues(nodeToPreprocess, node);
                if (!valuesList.isEmpty()) {
                    return valuesList;
                }
            }
        }

        return valuesList;
    }

    /**
     * Extracts values from an array whose path is specified in the mapping and returns a list
     * of extracted values
     *
     * @param node        - Node from which to extract the values
     * @param mappingNode - Mapping node holding the path to the array
     * @return - list of string values of the array
     * @throws MappingException - In case of mapping error
     */
    private static List<String> extractArrayValues(JsonNode node, JsonNode mappingNode) throws MappingException {
        String arrayRootPath = mappingNode.path("array-root").textValue();
        JsonNode arrayNode = node.at(arrayRootPath);

        List<String> values = new ArrayList<>();

        if (!arrayNode.isArray()) {
            String err = "The field " + arrayRootPath + " must be an array!";
            LOGGER.severe(err);
            throw new MappingException(err);
        }

        for (JsonNode valueNode : arrayNode) {
            values.add(valueNode.textValue());
        }

        return values;
    }

    // TODO: Edit for multiple options
    public static ArrayNode getNodesList(JsonNode mappingRoot, MappedFields field, JsonNode nodeToPreprocess) throws MappingException {
        JsonNode mappingNode = mappingRoot.path(field.value);

        ArrayNode res = null;

        if (!mappingNode.isArray()) {
            LOGGER.severe("Mapping field not an array");
            throw new MappingException("Mapping field not an array");
        }

        for (JsonNode node : mappingNode) {
            res = extractNodeByMapping(nodeToPreprocess, node);
        }

        return res;
    }

    /**
     * Creates a list of nodes according to specified mapping
     *
     * @param node
     * @param mappingNode
     * @return
     * @throws MappingException
     */
    public static ArrayNode extractNodeByMapping(JsonNode node, JsonNode mappingNode) throws MappingException {
        String arrayRootPath = mappingNode.path("array-root").textValue();
        JsonNode arrayNode = node.at(arrayRootPath);
        if (!arrayNode.isArray()) {
            String err = "The field " + arrayRootPath + " must be an array!";
            LOGGER.warning(err);
            return null;
            //throw new MappingException(err);
        }

        List<JsonNode> res = new ArrayList<>();

        for (JsonNode valueNode : arrayNode) {
            ObjectNode createdNode = JsonNodeFactory.instance.objectNode();

            for (Iterator<String> it = mappingNode.fieldNames(); it.hasNext(); ) {
                String key = it.next();
                String path = mappingNode.path(key).textValue();
                JsonNode n = valueNode.at(path);

                // In case of array
                if (mappingNode.path(key).isArray()) {
                    String subArrayPath = mappingNode.path(key).get(0).path("array-root").textValue();
                    if (subArrayPath != null) {
                        n = valueNode.at(subArrayPath);
                        createdNode.set(key, n);
                    } else {
                        // Neni tam array-root, spojime
                        String name = getMergedValue(mappingNode.path(key), valueNode);
                        createdNode.put(key, name);
                    }

                } else {
                    if (n.textValue() != null) {
                        createdNode.put(key, n.textValue());
                    }
                }
            }
            res.add(createdNode);
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.valueToTree(res);

        return array;
    }
}
