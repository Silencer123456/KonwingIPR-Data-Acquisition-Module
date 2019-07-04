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

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns a list of merged strings values from array node with paths.
     *
     * @param valuePaths - List of paths to Json nodes containing values
     * @param node       - The json node array in which we want to search the path
     * @return The merged list of strings values from multiple fields
     */
    private static List<String> getMergedValues(JsonNode valuePaths, JsonNode node) {
        List<String> mergedValues = new ArrayList<>();

        // Iterate elements of the array TODO: check if it is array
        for (JsonNode arrayElement : node) {
            //getMergedValue(valuePaths, arrayElement);

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

    /**
     * Wraps the putArray function which puts an array to a json node
     * with the specified name
     *
     * @param node  - Node to which to put the array
     * @param array - Array to put to the node
     */
    public static void putJsonArray(JsonNode node, ArrayNode array, String arrayName) {
        if (array == null || array.isMissingNode()) {
            //LOGGER.warning("Array " + arrayName + " is empty. Empty: " + empty + ", full: " + full);
            return;
        }
        ((ObjectNode) node).putArray(arrayName).addAll(array);
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

    /**
     * Iterates the specified path nodes in the mapping and for each path node
     * extracts an array node containing all the fields specified by the mapping
     *
     * @param mappingRoot      -- The root node of the mapping structure
     * @param field            - The field to search in the mapping (e.g. authors)
     * @param nodeToPreprocess - The node from which we want to extract information
     * @return - Array node with all the fields specified by the mapping
     * @throws MappingException
     */
    public static ArrayNode getNodesArrayWithOptions(JsonNode mappingRoot, MappedFields field, JsonNode nodeToPreprocess) throws MappingException {
        JsonNode mappingNode = mappingRoot.path(field.value);

        ArrayNode res = null;

        if (!mappingNode.isArray()) {
            LOGGER.severe("Mapping field is not an array");
            throw new MappingException("Mapping field is not an array");
        }

        int count = 0;
        for (JsonNode node : mappingNode) {
            if (count > 0) {
                System.out.println();
            }
            res = createArrayFromMapping(nodeToPreprocess, node);
            if (res != null) return res;

            count++;
        }

        return res;
    }

    /**
     * TODO: Refactor, documentation
     * Creates an array node containing all the fields specified by the mapping
     * Max depth of traversing the json structure is 2
     *
     * @param nodeToPreprocess
     * @param mappingNode      Node from the mapping containing fields to be included in the final array along with paths for the nodeToPreprocess node
     * @return
     * @throws MappingException
     */
    private static ArrayNode createArrayFromMapping(JsonNode nodeToPreprocess, JsonNode mappingNode) throws MappingException {
        String arrayRootPath = mappingNode.path("array-root").textValue(); // get the array-root field , which specifies the root of the array, containing the relevant data
        JsonNode arrayNode = nodeToPreprocess.at(arrayRootPath); // We navigate in the target node to the path specified by the array-root

        List<JsonNode> resultList = new ArrayList<>();

        if (arrayNode.isMissingNode()) {
            return null;
        }

        if (!arrayNode.isArray()) {
            ObjectNode createdNode = createNodeFromMapping(mappingNode, arrayNode); // Prepare a new node, where we will store the values from the array, but according to the mapping
            resultList.add(createdNode);
        } else {
            for (JsonNode element : arrayNode) { // Iterate over the elements of the array node that we got from the path specified in the array-root
                ObjectNode createdNode = createNodeFromMapping(mappingNode, element); // Prepare a new node, where we will store the values from the array, but according to the mapping
                resultList.add(createdNode);
            }
        }

        return mapper.valueToTree(resultList);
    }

    /**
     * Creates a single node according to the specified mapping
     *
     * @param mappingNode - The mapping node containing fields to be included in the created node
     * @param element     - A single element of the array from the target node where to look for values, where the mapping paths point to
     */
    private static ObjectNode createNodeFromMapping(JsonNode mappingNode, JsonNode element) {
        ObjectNode createdNode = JsonNodeFactory.instance.objectNode(); // Prepare a new node, where we will store the values from the array, but according to the mapping

        for (Iterator<String> it = mappingNode.fieldNames(); it.hasNext(); ) { // Iterate over all the fields in the mapping node
            String fieldName = it.next(); // get the name of the field in the mapping
            JsonNode fieldPathNode = mappingNode.path(fieldName);
            String fieldPath = fieldPathNode.textValue(); // get the path for that field
            JsonNode field = element.at(fieldPath); // Get single field node from the target node

            // In case of array
            if (fieldPathNode.isArray()) { // The mapping field can be specified as an array, which can contain another array-root
                String subArrayPath = fieldPathNode.get(0).path("array-root").textValue();
                if (subArrayPath != null) {
                    field = element.at(subArrayPath);
                    createdNode.set(fieldName, field);
                } else {
                    // Neni tam array-root, spojime
                    String name = getMergedValue(fieldPathNode, element);
                    createdNode.put(fieldName, name);
                }

            } else {
                if (field.textValue() != null) {
                    createdNode.put(fieldName, field.textValue());
                }
            }
        }

        return createdNode;
    }


    /**
     * NOT USED, USE getNodesArrayWithOptions INSTEAD
     * Gets a list of String values from a node specified in the mapping. The mapping node can be an array
     * (have multiple mappings for the same field -- mainly if the data source changes structure over time)
     *
     * @param mappingRoot      - Root mapping node
     * @param field            - Field in the mapping to look for
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
}
